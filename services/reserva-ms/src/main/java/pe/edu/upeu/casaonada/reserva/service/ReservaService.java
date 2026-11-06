package pe.edu.upeu.casaonada.reserva.service;

import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.casaonada.reserva.domain.EstadoReserva;
import pe.edu.upeu.casaonada.reserva.domain.Reserva;
import pe.edu.upeu.casaonada.reserva.dto.*;
import pe.edu.upeu.casaonada.reserva.exception.BusinessRuleException;
import pe.edu.upeu.casaonada.reserva.exception.ResourceNotFoundException;
import pe.edu.upeu.casaonada.reserva.integration.PropiedadIntegrationService;
import pe.edu.upeu.casaonada.reserva.mapper.ReservaMapper;
import pe.edu.upeu.casaonada.reserva.repository.ReservaRepository;

@Service
public class ReservaService {
  private final ReservaRepository repo;
  private final ReservaMapper mapper;
  private final PropiedadIntegrationService propiedadIntegration;

  public ReservaService(
      ReservaRepository repo,
      ReservaMapper mapper,
      PropiedadIntegrationService propiedadIntegration) {
    this.repo = repo;
    this.mapper = mapper;
    this.propiedadIntegration = propiedadIntegration;
  }

  @Transactional
  public ReservaResponse crear(ReservaRequest req) {
    if (req.clienteId() == null) {
      throw new BusinessRuleException("No se pudo determinar el cliente autenticado");
    }
    if (repo.existsByPropiedadIdAndEstadoIn(
        req.propiedadId(), List.of(EstadoReserva.PENDIENTE, EstadoReserva.ACTIVA))) {
      throw new BusinessRuleException("La propiedad ya tiene una reserva activa o pendiente");
    }

    var propiedad = propiedadIntegration.validarReservable(req.propiedadId());
    if (!"DISPONIBLE".equalsIgnoreCase(propiedad.estado())) {
      throw new BusinessRuleException(
          "La propiedad no está disponible para reservar. Estado actual: " + propiedad.estado());
    }

    pe.edu.upeu.casaonada.shared.DomainRules.money(req.montoReserva(), "Adelanto");
    if (req.montoReserva().compareTo(propiedad.precio()) > 0)
      throw new BusinessRuleException("Adelanto superior al precio del inmueble");
    Reserva entidad = mapper.toEntity(req);
    entidad.setEstado(EstadoReserva.PENDIENTE);
    if (entidad.getFechaExpiracion().isBefore(OffsetDateTime.now())) {
      throw new BusinessRuleException("La fecha de expiración debe ser futura");
    }
    return mapper.toResponse(repo.save(entidad));
  }

  /** Datos del inmueble para el correo de confirmacion; null si el catalogo no responde. */
  public PropiedadDto propiedadParaCorreo(Long id) {
    try {
      return propiedadIntegration.validarReservable(id);
    } catch (RuntimeException e) {
      return null;
    }
  }

  @Transactional(readOnly = true)
  public List<ReservaResponse> listar() {
    return repo.findAll().stream().map(mapper::toResponse).toList();
  }

  @Transactional(readOnly = true)
  public List<ReservaResponse> listarPorCliente(Long clienteId) {
    return repo.findAllByClienteIdOrderByCreatedAtDesc(clienteId).stream()
        .map(mapper::toResponse)
        .toList();
  }

  @Transactional(readOnly = true)
  public ReservaResponse buscar(Long id) {
    return mapper.toResponse(entidad(id));
  }

  @Transactional(readOnly = true)
  public ReservaDetalleResponse detalle(Long id) {
    ReservaResponse reserva = buscar(id);
    PropiedadConsultaResult consulta = propiedadIntegration.consultar(reserva.propiedadId());
    return new ReservaDetalleResponse(
        reserva, consulta.propiedad(), consulta.degradado(), consulta.mensaje());
  }

  @Transactional
  public ReservaResponse actualizarEstado(Long id, EstadoReserva estado) {
    Reserva entidad =
        repo.locked(id).orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada"));
    if (entidad.getSagaId() != null)
      throw new BusinessRuleException("Cancele la orden para compensar la reserva asociada");
    if (estado != EstadoReserva.CANCELADA)
      throw new BusinessRuleException("La SAGA administra el estado operativo de la reserva");
    entidad.setEstado(estado);
    return mapper.toResponse(repo.save(entidad));
  }

  @Transactional
  public void eliminar(Long id) {
    var r =
        repo.locked(id).orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada"));
    if (r.getSagaId() != null)
      throw new BusinessRuleException("No se puede eliminar una reserva asociada a una orden");
    repo.delete(r);
  }

  private Reserva entidad(Long id) {
    return repo.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada: " + id));
  }
}
