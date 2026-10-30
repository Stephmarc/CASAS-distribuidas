package pe.edu.upeu.casaonada.orden.service;

import java.time.*;
import java.util.*;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import pe.edu.upeu.casaonada.orden.domain.*;
import pe.edu.upeu.casaonada.orden.dto.*;
import pe.edu.upeu.casaonada.orden.repository.*;
import pe.edu.upeu.casaonada.shared.*;

@Service
public class OrdenService {
  private final IdempotencyLocks locks;
  private final OrdenRepository repo;
  private final ConsultasRemotas consultas;
  private final Outbox outbox;

  public OrdenService(OrdenRepository r, ConsultasRemotas c, Outbox o, IdempotencyLocks l) {
    locks = l;
    repo = r;
    consultas = c;
    outbox = o;
  }

  @Transactional
  public Orden crear(OrdenRequest req, String key) {
    if (key == null || key.isBlank() || key.length() > 120)
      throw new IllegalArgumentException("X-Idempotency-Key es obligatorio (1-120 caracteres)");
    locks.lock("orden", key);
    var old = repo.findByIdempotencyKey(key);
    if (old.isPresent()) {
      var o = old.get();
      Identidad.propietario(o.clienteId);
      if (!o.reservaId.equals(req.reservaId()) || !o.moneda.equals(req.moneda()))
        throw new ResponseStatusException(HttpStatus.CONFLICT, "Clave reutilizada con otros datos");
      return o;
    }
    var r = consultas.reserva(req.reservaId());
    Identidad.propietario(r.clienteId());
    if (!Set.of("PENDIENTE", "ACTIVA").contains(r.estado())
        || !r.fechaExpiracion().isAfter(OffsetDateTime.now()))
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Reserva no vigente");
    var c = consultas.cliente(r.clienteId());
    if (!c.activo()) throw new ResponseStatusException(HttpStatus.CONFLICT, "Cliente inactivo");
    var p = consultas.propiedad(r.propiedadId());
    if (!"DISPONIBLE".equals(p.estado()))
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Propiedad no disponible");
    if (r.montoReserva().signum() <= 0 || r.montoReserva().compareTo(p.precio()) > 0)
      throw new ResponseStatusException(
          HttpStatus.CONFLICT, "Adelanto fuera del valor de la propiedad");
    Orden o = new Orden();
    o.total = p.precio();
    o.reservaId = r.id();
    o.propiedadId = r.propiedadId();
    o.clienteId = r.clienteId();
    o.monto = r.montoReserva();
    o.moneda = req.moneda();
    o.tipo = p.tipoOperacion();
    o.sagaId = UUID.randomUUID().toString();
    o.idempotencyKey = key;
    o.estado = EstadoOrden.VALIDANDO_RESERVA;
    o.traceId = Optional.ofNullable(MDC.get("traceId")).orElse(o.sagaId);
    o.createdAt = OffsetDateTime.now();
    o.updatedAt = o.createdAt;
    o.reservaExpiraAt = r.fechaExpiracion();
    o.deadline = earlier(o.createdAt.plusMinutes(2), o.reservaExpiraAt);
    repo.saveAndFlush(o);
    outbox.add(event(o, "ASOCIAR_RESERVA"));
    return o;
  }

  @Transactional(readOnly = true)
  public Orden buscar(Long id) {
    var o =
        repo.findById(id)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Orden no encontrada"));
    Identidad.propietario(o.clienteId);
    return o;
  }

  @Transactional(readOnly = true)
  public List<Orden> listar() {
    return Identidad.rol("ADMIN") || Identidad.rol("AGENTE")
        ? repo.findAll()
        : repo.findByClienteId(Identidad.cliente());
  }

  @Transactional
  public Orden cancelar(Long id) {
    var o = repo.locked(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    Identidad.propietario(o.clienteId);
    if (o.estado == EstadoOrden.CONFIRMADA)
      throw new ResponseStatusException(
          HttpStatus.CONFLICT, "Una orden confirmada requiere un proceso de devolucion separado");
    advance(o, "CANCELAR", "Cancelacion solicitada");
    return o;
  }

  public void advance(Orden o, String type, String motivo) {
    var change = SagaTransitions.apply(o.estado, type);
    if (change.estado() == o.estado) return;
    o.estado = change.estado();
    o.updatedAt = OffsetDateTime.now();
    o.deadline = o.updatedAt.plusMinutes(o.estado == EstadoOrden.ESPERANDO_PAGO ? 30 : 2);
    if (!o.estado.compensando()) o.deadline = earlier(o.deadline, o.reservaExpiraAt);
    if (o.estado.compensando()) o.motivo = motivo;
    repo.save(o);
    if (change.command() != null) outbox.add(event(o, change.command()));
  }

  private OffsetDateTime earlier(OffsetDateTime a, OffsetDateTime b) {
    return a.isBefore(b) ? a : b;
  }

  public OperacionEvent event(Orden o, String type) {
    return new OperacionEvent(
        UUID.randomUUID().toString(),
        o.sagaId,
        type,
        o.id,
        o.reservaId,
        o.propiedadId,
        o.clienteId,
        o.monto,
        o.moneda,
        o.tipo,
        o.traceId,
        o.motivo,
        o.total);
  }
}
