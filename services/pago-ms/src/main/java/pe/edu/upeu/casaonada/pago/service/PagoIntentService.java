package pe.edu.upeu.casaonada.pago.service;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import pe.edu.upeu.casaonada.pago.domain.*;
import pe.edu.upeu.casaonada.pago.dto.*;
import pe.edu.upeu.casaonada.pago.integration.MercadoPagoGateway;
import pe.edu.upeu.casaonada.pago.repository.PagoRepository;
import pe.edu.upeu.casaonada.shared.*;

@Service
public class PagoIntentService {
  private final PagoRepository repo;
  private final PagoSagaGuard guard;
  private final ConsultasRemotas consultas;
  private final Outbox outbox;
  private final IdempotencyLocks locks;
  private final EntityManager em;

  public PagoIntentService(
      PagoRepository r,
      PagoSagaGuard g,
      ConsultasRemotas c,
      Outbox o,
      IdempotencyLocks l,
      EntityManager e) {
    repo = r;
    guard = g;
    consultas = c;
    outbox = o;
    locks = l;
    em = e;
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public Long reserve(PagoRequest request, String key) {
    locks.lock("pago", key);
    DomainRules.money(request.monto(), "Adelanto");
    String hash = PaymentFingerprint.of(request);
    var old = repo.findByIdempotencyKey(key);
    if (old.isPresent()) {
      var p = old.get();
      Identidad.propietario(
          p.getClienteId() != null ? p.getClienteId() : guard.context(p.getOrdenId()).clienteId());
      if (!p.getOrdenId().equals(request.ordenId())
          || p.getMonto().compareTo(request.monto()) != 0
          || !p.getMoneda().equals(request.moneda())
          || p.getRequestHash() != null && !p.getRequestHash().equals(hash))
        throw conflict("Clave reutilizada con otros datos");
      return p.getId();
    }
    var order = consultas.orden(request.ordenId());
    Identidad.propietario(order.clienteId());
    var event = PagoSagaGuard.context(order);
    if (guard.lock(event) || !order.estado().equals("ESPERANDO_PAGO"))
      throw conflict("La orden no admite otro pago");
    if (order.monto().compareTo(request.monto()) != 0 || !order.moneda().equals(request.moneda()))
      throw conflict("Monto o moneda distintos de la orden");
    if (repo.findByOrdenId(order.id()).stream()
        .anyMatch(p -> !PaymentRules.terminal(p.getEstado())))
      throw conflict("La orden ya tiene un pago vigente");
    var p = new Pago();
    p.setOrdenId(order.id());
    p.setClienteId(order.clienteId());
    p.setMonto(order.monto());
    p.setMoneda(order.moneda());
    p.setProveedor("MERCADO_PAGO");
    p.setIdempotencyKey(key);
    p.setEstado(EstadoPago.CREADO);
    p.setRequestHash(hash);
    p.setExternalReference("casa-" + UUID.randomUUID());
    repo.saveAndFlush(p);
    return p.getId();
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public boolean claim(Long id) {
    var p = entity(id);
    var event = guard.context(p.getOrdenId());
    boolean cancelled = guard.lock(event);
    em.refresh(p, LockModeType.PESSIMISTIC_WRITE);
    if (cancelled) {
      if (p.getEstado() == EstadoPago.CREADO && p.getSubmissionStartedAt() == null) {
        p.setEstado(EstadoPago.CANCELADO);
        repo.save(p);
      }
      return false;
    }
    if (!"MERCADO_PAGO".equals(p.getProveedor())
        || p.getEstado() != EstadoPago.CREADO
        || p.getSubmissionStartedAt() != null) return false;
    p.setSubmissionStartedAt(OffsetDateTime.now());
    repo.saveAndFlush(p);
    return true;
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
  public Pago snapshot(Long id) {
    return entity(id);
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void complete(Long id, MercadoPagoGateway.Resultado result) {
    var p = entity(id);
    var event = guard.context(p.getOrdenId());
    guard.lock(event);
    em.refresh(p, LockModeType.PESSIMISTIC_WRITE);
    if (PaymentRules.terminal(p.getEstado())) return;
    PaymentRules.change(p.getEstado(), result.estado());
    boolean changed = p.getEstado() != result.estado();
    p.setProviderPaymentId(result.providerPaymentId());
    p.setEstado(result.estado());
    p.setLastProviderError(null);
    repo.save(p);
    if (changed && p.getEstado() == EstadoPago.APROBADO) outbox.add(event.next("PAGO_APROBADO"));
    else if (changed && PaymentRules.terminal(p.getEstado()))
      outbox.add(event.next("PAGO_RECHAZADO", "Proveedor rechazo o cancelo el pago"));
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void failed(Long id, boolean definitive, String error) {
    var p = entity(id);
    var event = guard.context(p.getOrdenId());
    guard.lock(event);
    em.refresh(p, LockModeType.PESSIMISTIC_WRITE);
    if (p.getEstado() != EstadoPago.CREADO) return;
    p.setLastProviderError(error);
    p.setReconcileAttempts(p.getReconcileAttempts() + 1);
    if (definitive) {
      p.setEstado(EstadoPago.RECHAZADO);
      outbox.add(event.next("PAGO_RECHAZADO", "Solicitud rechazada por el proveedor"));
    }
    repo.save(p);
  }

  private Pago entity(Long id) {
    return repo.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pago no encontrado"));
  }

  private ResponseStatusException conflict(String text) {
    return new ResponseStatusException(HttpStatus.CONFLICT, text);
  }
}
