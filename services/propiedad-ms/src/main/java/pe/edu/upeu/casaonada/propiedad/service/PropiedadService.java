package pe.edu.upeu.casaonada.propiedad.service;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.casaonada.propiedad.domain.*;
import pe.edu.upeu.casaonada.propiedad.dto.*;
import pe.edu.upeu.casaonada.propiedad.exception.ResourceNotFoundException;
import pe.edu.upeu.casaonada.propiedad.mapper.PropiedadMapper;
import pe.edu.upeu.casaonada.propiedad.repository.PropiedadRepository;

@Service
public class PropiedadService {
  private final pe.edu.upeu.casaonada.shared.ConsultasRemotas consultas;
  private final PropiedadRepository repo;
  private final PropiedadMapper mapper;

  public PropiedadService(
      PropiedadRepository r, PropiedadMapper m, pe.edu.upeu.casaonada.shared.ConsultasRemotas c) {
    consultas = c;
    repo = r;
    mapper = m;
  }

  @Transactional
  public PropiedadResponse crear(PropiedadRequest r) {
    validate(r);
    return mapper.toResponse(repo.save(mapper.toEntity(r)));
  }

  @Transactional(readOnly = true)
  public List<PropiedadResponse> listar(
      String ciudad, TipoOperacion tipo, EstadoPropiedad estado, BigDecimal min, BigDecimal max) {
    if (min != null && min.signum() < 0
        || max != null && max.signum() < 0
        || min != null && max != null && min.compareTo(max) > 0)
      throw new IllegalArgumentException("Rango de precios invalido");
    return repo.buscar(vacioANull(ciudad), tipo, estado, min, max).stream()
        .filter(this::visible)
        .map(mapper::toResponse)
        .toList();
  }

  @Transactional(readOnly = true)
  public PropiedadResponse buscar(Long id) {
    var p = entidad(id);
    if (!visible(p)) throw new ResourceNotFoundException("Propiedad no encontrada");
    return mapper.toResponse(p);
  }

  @Transactional
  public PropiedadResponse actualizar(Long id, PropiedadRequest r) {
    validate(r);
    Propiedad e = lockedEditable(id);
    mapper.update(e, r);
    return mapper.toResponse(repo.save(e));
  }

  @Transactional
  public PropiedadResponse cambiarEstado(Long id, EstadoPropiedad estado) {
    pe.edu.upeu.casaonada.shared.DomainRules.catalogState(estado.name());
    Propiedad e = lockedEditable(id);
    e.setEstado(estado);
    return mapper.toResponse(repo.save(e));
  }

  @Transactional
  public void eliminar(Long id) {
    var p = lockedEditable(id);
    p.setEstado(EstadoPropiedad.INACTIVA);
    repo.save(p);
  }

  private void validate(PropiedadRequest r) {
    if (r.agenteId() != null && !"ACTIVO".equals(consultas.agente(r.agenteId()).estado()))
      throw new IllegalArgumentException("El agente asignado no esta activo");
    pe.edu.upeu.casaonada.shared.DomainRules.money(r.precio(), "Precio");
    pe.edu.upeu.casaonada.shared.DomainRules.catalogState(r.estado().name());
    if (r.habitaciones() < 0 || r.banos() < 0 || r.areaM2().signum() <= 0)
      throw new IllegalArgumentException("Dimensiones del inmueble invalidas");
  }

  private Propiedad lockedEditable(Long id) {
    var p =
        repo.locked(id).orElseThrow(() -> new ResourceNotFoundException("Propiedad no encontrada"));
    if (p.getSagaId() != null)
      throw new pe.edu.upeu.casaonada.propiedad.exception.BusinessRuleException(
          "La propiedad esta asociada a una operacion; use su SAGA");
    return p;
  }

  private boolean visible(Propiedad p) {
    return pe.edu.upeu.casaonada.shared.Identidad.rol("ADMIN")
        || pe.edu.upeu.casaonada.shared.Identidad.rol("AGENTE")
        || p.getEstado() != EstadoPropiedad.BORRADOR && p.getEstado() != EstadoPropiedad.INACTIVA;
  }

  private Propiedad entidad(Long id) {
    return repo.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Propiedad no encontrada: " + id));
  }

  private String vacioANull(String s) {
    return s == null || s.isBlank() ? null : s;
  }
}
