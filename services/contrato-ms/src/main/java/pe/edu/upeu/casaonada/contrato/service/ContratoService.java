package pe.edu.upeu.casaonada.contrato.service;

import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.casaonada.contrato.domain.*;
import pe.edu.upeu.casaonada.contrato.dto.*;
import pe.edu.upeu.casaonada.contrato.exception.*;
import pe.edu.upeu.casaonada.contrato.mapper.ContratoMapper;
import pe.edu.upeu.casaonada.contrato.repository.ContratoRepository;
import pe.edu.upeu.casaonada.shared.Identidad;

@Service
public class ContratoService {
  private final pe.edu.upeu.casaonada.shared.ConsultasRemotas consultas;
  private final ContratoRepository repo;
  private final ContratoMapper mapper;

  public ContratoService(
      ContratoRepository r, ContratoMapper m, pe.edu.upeu.casaonada.shared.ConsultasRemotas c) {
    consultas = c;
    repo = r;
    mapper = m;
  }

  public ContratoResponse crear(ContratoRequest r) {
    throw new BusinessRuleException("El contrato se genera por SAGA despues del pago aprobado");
  }

  @Transactional(readOnly = true)
  public List<ContratoResponse> listar() {
    return repo.findAll().stream()
        .filter(
            c ->
                Identidad.rol("ADMIN")
                    || Identidad.rol("AGENTE")
                    || c.getClienteId().equals(Identidad.cliente()))
        .map(mapper::toResponse)
        .toList();
  }

  @Transactional(readOnly = true)
  public ContratoResponse buscar(Long id) {
    var c = entidad(id);
    Identidad.propietario(c.getClienteId());
    return mapper.toResponse(c);
  }

  public ContratoResponse actualizar(Long id, ContratoRequest r) {
    throw new BusinessRuleException(
        "Los datos financieros del contrato provienen de la orden y no son editables");
  }

  @Transactional
  public ContratoResponse cambiarEstado(Long id, EstadoContrato estado) {
    var c =
        repo.locked(id).orElseThrow(() -> new ResourceNotFoundException("Contrato no encontrado"));
    if (estado == EstadoContrato.ACEPTADO && !Identidad.rol("ADMIN") && !Identidad.rol("CLIENTE"))
      throw new org.springframework.security.access.AccessDeniedException(
          "Solo el cliente o un administrador puede registrar la aceptacion");
    Identidad.propietario(c.getClienteId());
    if (!"CONFIRMADA".equals(consultas.orden(c.getOrdenId()).estado()))
      throw new BusinessRuleException("La orden aun no esta confirmada");
    boolean valid =
        switch (c.getEstado()) {
          case GENERADO -> estado == EstadoContrato.PENDIENTE_ACEPTACION;
          case PENDIENTE_ACEPTACION -> estado == EstadoContrato.ACEPTADO;
          case ACEPTADO -> estado == EstadoContrato.VIGENTE;
          case VIGENTE -> estado == EstadoContrato.FINALIZADO;
          default -> false;
        };
    if (!valid) throw new BusinessRuleException("Transicion de contrato no permitida");
    c.setEstado(estado);
    return mapper.toResponse(repo.save(c));
  }

  @Transactional
  public ContratoResponse aceptar(Long id) {
    return cambiarEstado(id, EstadoContrato.ACEPTADO);
  }

  private Contrato entidad(Long id) {
    return repo.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Contrato no encontrado"));
  }
}
