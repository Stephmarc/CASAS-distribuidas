package pe.edu.upeu.casaonada.visita.service;

import java.time.OffsetDateTime;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.casaonada.shared.*;
import pe.edu.upeu.casaonada.visita.domain.*;
import pe.edu.upeu.casaonada.visita.dto.*;
import pe.edu.upeu.casaonada.visita.exception.*;
import pe.edu.upeu.casaonada.visita.mapper.VisitaMapper;
import pe.edu.upeu.casaonada.visita.repository.VisitaRepository;

@Service
public class VisitaService {
  private static final List<EstadoVisita> ACTIVE =
      List.of(EstadoVisita.SOLICITADA, EstadoVisita.CONFIRMADA, EstadoVisita.REPROGRAMADA);
  private final VisitaRepository repo;
  private final VisitaMapper mapper;
  private final ConsultasRemotas consultas;

  public VisitaService(VisitaRepository r, VisitaMapper m, ConsultasRemotas c) {
    repo = r;
    mapper = m;
    consultas = c;
  }

  @Transactional
  public VisitaResponse crear(VisitaRequest r) {
    validate(r, null);
    var e = mapper.toEntity(r);
    e.setEstado(EstadoVisita.SOLICITADA);
    return mapper.toResponse(repo.save(e));
  }

  @Transactional(readOnly = true)
  public List<VisitaResponse> listar() {
    return (Identidad.rol("ADMIN") || Identidad.rol("AGENTE")
            ? repo.findAll()
            : repo.findByClienteId(Identidad.cliente()))
        .stream().map(mapper::toResponse).toList();
  }

  @Transactional(readOnly = true)
  public VisitaResponse buscar(Long id) {
    var e = entity(id);
    Identidad.propietario(e.getClienteId());
    return mapper.toResponse(e);
  }

  @Transactional
  public VisitaResponse actualizar(Long id, VisitaRequest r) {
    var e = locked(id);
    if (!e.getClienteId().equals(r.clienteId()) || !e.getPropiedadId().equals(r.propiedadId()))
      throw new BusinessRuleException("La reprogramacion no cambia al cliente ni el inmueble");
    DomainRules.transitionVisit(e.getEstado().name(), "REPROGRAMADA");
    validate(r, id);
    mapper.update(e, r);
    e.setEstado(EstadoVisita.REPROGRAMADA);
    return mapper.toResponse(repo.save(e));
  }

  @Transactional
  public VisitaResponse cambiarEstado(Long id, EstadoVisita state) {
    var e = locked(id);
    DomainRules.transitionVisit(e.getEstado().name(), state.name());
    if ((state == EstadoVisita.REALIZADA || state == EstadoVisita.NO_ASISTIO)
        && e.getFechaHora().isAfter(OffsetDateTime.now()))
      throw new BusinessRuleException("La visita todavia no ha ocurrido");
    if ((state == EstadoVisita.CONFIRMADA || state == EstadoVisita.REPROGRAMADA)
        && !e.getFechaHora().isAfter(OffsetDateTime.now()))
      throw new BusinessRuleException("Debe reprogramar la visita a una fecha futura");
    e.setEstado(state);
    return mapper.toResponse(repo.save(e));
  }

  @Transactional
  public VisitaResponse cancelar(Long id) {
    var e = locked(id);
    Identidad.propietario(e.getClienteId());
    DomainRules.transitionVisit(e.getEstado().name(), "CANCELADA");
    e.setEstado(EstadoVisita.CANCELADA);
    return mapper.toResponse(repo.save(e));
  }

  @Transactional
  public void eliminar(Long id) {
    repo.delete(locked(id));
  }

  private void validate(VisitaRequest r, Long exclude) {
    if (!r.fechaHora().isAfter(OffsetDateTime.now()))
      throw new BusinessRuleException("La visita requiere una fecha futura");
    if (!consultas.cliente(r.clienteId()).activo())
      throw new BusinessRuleException("Cliente inactivo");
    if (!"ACTIVO".equals(consultas.agente(r.agenteId()).estado()))
      throw new BusinessRuleException("Agente no disponible");
    if (!Set.of("PUBLICADA", "DISPONIBLE").contains(consultas.propiedad(r.propiedadId()).estado()))
      throw new BusinessRuleException("El inmueble no admite visitas");
    if (repo.findByFechaHoraAndEstadoIn(r.fechaHora(), ACTIVE).stream()
        .anyMatch(
            v ->
                !Objects.equals(v.getId(), exclude)
                    && (v.getAgenteId().equals(r.agenteId())
                        || v.getClienteId().equals(r.clienteId()))))
      throw new BusinessRuleException("El agente o el cliente ya tiene una visita en ese horario");
  }

  private Visita entity(Long id) {
    return repo.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Visita no encontrada"));
  }

  private Visita locked(Long id) {
    return repo.locked(id).orElseThrow(() -> new ResourceNotFoundException("Visita no encontrada"));
  }
}
