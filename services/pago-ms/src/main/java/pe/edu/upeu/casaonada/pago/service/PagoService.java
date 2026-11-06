package pe.edu.upeu.casaonada.pago.service;

import java.util.*;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import pe.edu.upeu.casaonada.pago.domain.*;
import pe.edu.upeu.casaonada.pago.dto.*;
import pe.edu.upeu.casaonada.pago.integration.MercadoPagoGateway;
import pe.edu.upeu.casaonada.pago.repository.PagoRepository;
import pe.edu.upeu.casaonada.shared.*;

@Service
public class PagoService {
  private final jakarta.persistence.EntityManager em;
  private final IdempotencyLocks locks;
  private final PagoRepository repo;
  private final ObjectProvider<MercadoPagoGateway> mp;
  private final ConsultasRemotas consultas;
  private final PagoSagaGuard guard;
  private final Outbox outbox;
  private final boolean sandbox;
  private final pe.edu.upeu.casaonada.pago.repository.PagoComprobanteRepository comprobantes;

  public PagoService(
      PagoRepository r,
      ObjectProvider<MercadoPagoGateway> m,
      ConsultasRemotas c,
      PagoSagaGuard g,
      Outbox o,
      @Value("${app.pagos.sandbox.enabled:false}") boolean s,
      jakarta.persistence.EntityManager e,
      IdempotencyLocks l,
      pe.edu.upeu.casaonada.pago.repository.PagoComprobanteRepository cr) {
    comprobantes = cr;
    em = e;
    locks = l;
    repo = r;
    mp = m;
    consultas = c;
    guard = g;
    outbox = o;
    sandbox = s;
  }

  @Transactional
  public PagoResponse crear(PagoRequest r, String key) {
    if (key == null || key.isBlank() || key.length() > 120)
      throw new IllegalArgumentException("X-Idempotency-Key es obligatorio");
    locks.lock("pago", key);
    DomainRules.money(r.monto(), "Adelanto");
    var old = repo.findByIdempotencyKey(key);
    if (old.isPresent()) {
      var p = old.get();
      if (!Identidad.rol("ADMIN") && !Identidad.rol("AGENTE"))
        Identidad.propietario(guard.context(p.getOrdenId()).clienteId());
      if (!p.getOrdenId().equals(r.ordenId())
          || p.getMonto().compareTo(r.monto()) != 0
          || !p.getMoneda().equals(r.moneda())
          || p.getRequestHash() != null && !p.getRequestHash().equals(PaymentFingerprint.of(r)))
        throw conflict("Clave reutilizada con otros datos");
      return map(p);
    }
    var o = consultas.orden(r.ordenId());
    Identidad.propietario(o.clienteId());
    var e = PagoSagaGuard.context(o);
    if (guard.lock(e)) throw conflict("Saga cancelada");
    if (!o.estado().equals("ESPERANDO_PAGO")) throw conflict("La orden no esta esperando un pago");
    if (o.monto().compareTo(r.monto()) != 0 || !o.moneda().equals(r.moneda()))
      throw conflict("Monto o moneda distintos del adelanto de la orden");
    if (repo.findByOrdenId(o.id()).stream()
        .anyMatch(
            p ->
                Set.of(EstadoPago.CREADO, EstadoPago.PENDIENTE, EstadoPago.APROBADO)
                    .contains(p.getEstado()))) throw conflict("La orden ya tiene un pago vigente");
    Pago p = new Pago();
    p.setOrdenId(o.id());
    p.setClienteId(o.clienteId());
    p.setRequestHash(PaymentFingerprint.of(r));
    p.setMonto(o.monto());
    p.setMoneda(o.moneda());
    p.setIdempotencyKey(key);
    p.setEstado(EstadoPago.CREADO);
    var gateway = mp.getIfAvailable();
    if (gateway != null)
      throw new IllegalStateException("Los pagos externos requieren una intencion persistente");
    else if (sandbox) {
      p.setProveedor("SANDBOX_LOCAL");
      p.setEstado(EstadoPago.PENDIENTE);
    } else
      throw new ResponseStatusException(
          HttpStatus.SERVICE_UNAVAILABLE,
          "Configure Mercado Pago o habilite sandbox local explicitamente");
    repo.saveAndFlush(p);
    emit(p, e);
    return map(p);
  }

  @Transactional
  public PagoResponse simular(Long id, EstadoPago estado) {
    if (!sandbox) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    var p = entidad(id);
    var e = guard.context(p.getOrdenId());
    if (guard.lock(e)) throw conflict("Saga cancelada");
    em.refresh(p, jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
    if (!p.getProveedor().equals("SANDBOX_LOCAL"))
      throw conflict("El simulador solo acepta pagos locales");
    if (estado != EstadoPago.APROBADO && estado != EstadoPago.RECHAZADO)
      throw new IllegalArgumentException("Estado de simulacion invalido");
    if (p.getEstado() == estado) return map(p);
    if (p.getEstado() != EstadoPago.PENDIENTE) throw conflict("Pago ya resuelto");
    p.setEstado(estado);
    repo.save(p);
    emit(p, e);
    return map(p);
  }

  public record Comprobante(String tipo, byte[] datos) {}

  /** El cliente declara su pago (Yape, Plin, transferencia) y adjunta la captura. */
  @Transactional
  public PagoResponse comprobante(Long id, String metodo, String nro, String tipo, byte[] datos) {
    var p = entidad(id);
    Identidad.propietario(p.getClienteId());
    if (!Set.of("YAPE", "PLIN", "TRANSFERENCIA", "DEPOSITO").contains(metodo))
      throw new IllegalArgumentException("Metodo de pago invalido");
    if (nro == null || !nro.matches("[A-Za-z0-9-]{4,40}"))
      throw new IllegalArgumentException("Numero de operacion invalido");
    if (!Set.of("image/png", "image/jpeg", "image/webp").contains(tipo))
      throw new IllegalArgumentException("La captura debe ser PNG, JPG o WEBP");
    if (datos.length == 0 || datos.length > 3 * 1024 * 1024)
      throw new IllegalArgumentException("La captura debe pesar entre 1 byte y 3 MB");
    var e = guard.context(p.getOrdenId());
    if (guard.lock(e)) throw conflict("Saga cancelada");
    em.refresh(p, jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
    if (!p.getProveedor().equals("SANDBOX_LOCAL")) throw conflict("Solo aplica a pagos manuales");
    if (p.getEstado() != EstadoPago.PENDIENTE) throw conflict("El pago ya fue resuelto");
    var c = comprobantes.findById(id).orElseGet(PagoComprobante::new);
    c.pagoId = id;
    c.tipo = tipo;
    c.datos = datos;
    comprobantes.save(c);
    p.setMetodo(metodo);
    p.setNroOperacion(nro);
    p.setComprobanteAt(java.time.OffsetDateTime.now());
    p.setMotivoRevision(null);
    repo.save(p);
    return map(p);
  }

  @Transactional(readOnly = true)
  public Comprobante verComprobante(Long id) {
    buscar(id);
    var c =
        comprobantes
            .findById(id)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sin comprobante"));
    return new Comprobante(c.tipo, c.datos);
  }

  /** El agente (o admin) revisa la captura: APROBAR, OBSERVAR (pide otra captura) o RECHAZAR. */
  @Transactional
  public PagoResponse revisar(Long id, String accion, String motivo) {
    var p = entidad(id);
    var e = guard.context(p.getOrdenId());
    if (guard.lock(e)) throw conflict("Saga cancelada");
    em.refresh(p, jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
    if (!p.getProveedor().equals("SANDBOX_LOCAL")) throw conflict("Solo aplica a pagos manuales");
    if (p.getComprobanteAt() == null) throw conflict("El cliente aun no envio la captura");
    if (p.getEstado() != EstadoPago.PENDIENTE) throw conflict("Pago ya resuelto");
    if (!Set.of("APROBAR", "OBSERVAR", "RECHAZAR").contains(accion))
      throw new IllegalArgumentException("Accion invalida");
    var m = motivo == null ? "" : motivo.trim();
    if (!accion.equals("APROBAR") && m.isEmpty())
      throw new IllegalArgumentException("Indica el motivo");
    p.setMotivoRevision(m.isEmpty() ? null : m.substring(0, Math.min(200, m.length())));
    switch (accion) {
      case "APROBAR" -> p.setEstado(EstadoPago.APROBADO);
      case "RECHAZAR" -> p.setEstado(EstadoPago.RECHAZADO);
      default -> {
        // observado: el pago sigue pendiente y el cliente puede enviar otra captura
        p.setComprobanteAt(null);
        comprobantes.deleteById(id);
      }
    }
    repo.save(p);
    emit(p, e);
    return map(p);
  }

  @Transactional
  public void compensar(OperacionEvent e) {
    guard.lock(e);
    guard.cancel(e);
    for (var p : repo.findByOrdenId(e.ordenId())) {
      em.refresh(p, jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
      if (p.getProveedor().equals("MERCADO_PAGO")
          && p.getProviderPaymentId() == null
          && !PaymentRules.terminal(p.getEstado())) {
        var recovery = PaymentRules.recovery(null, p.getSubmissionStartedAt() != null);
        if (recovery == PaymentRules.Recovery.SEARCH_REFERENCE) {
          var found = mp.getObject().buscarPorReferencia(p.getExternalReference(), p.getMonto());
          if (found.isEmpty())
            throw new IllegalStateException(
                "Pago externo pendiente de conciliacion; compensacion no confirmada");
          p.setProviderPaymentId(found.get().providerPaymentId());
          p.setEstado(found.get().estado());
        } else if (p.getExternalReference() != null) {
          p.setEstado(EstadoPago.CANCELADO);
        } else
          throw new IllegalStateException(
              "Pago externo antiguo sin referencia requiere conciliacion");
      }
      if (p.getEstado() == EstadoPago.APROBADO) {
        if (p.getProveedor().equals("MERCADO_PAGO"))
          mp.getObject().reembolsar(p.getProviderPaymentId(), e.sagaId() + "-refund-" + p.getId());
        p.setEstado(EstadoPago.REEMBOLSADO);
      } else if (p.getEstado() == EstadoPago.PENDIENTE || p.getEstado() == EstadoPago.CREADO) {
        if (p.getProveedor().equals("MERCADO_PAGO"))
          p.setEstado(
              mp.getObject()
                  .cancelar(p.getProviderPaymentId(), e.sagaId() + "-refund-" + p.getId()));
        else p.setEstado(EstadoPago.CANCELADO);
      }
      repo.save(p);
    }
    outbox.add(e.next("PAGO_REEMBOLSADO"));
  }

  @Transactional
  public void reconciliar(Long id) {
    var p = entidad(id);
    var e = guard.context(p.getOrdenId());
    if (guard.lock(e) || !p.getProveedor().equals("MERCADO_PAGO")) return;
    em.refresh(p, jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
    if (PaymentRules.terminal(p.getEstado()) || p.getEstado() == EstadoPago.APROBADO) return;
    java.util.Optional<MercadoPagoGateway.Resultado> found =
        p.getProviderPaymentId() != null
            ? java.util.Optional.of(mp.getObject().consultar(p.getProviderPaymentId()))
            : p.getSubmissionStartedAt() != null
                ? mp.getObject().buscarPorReferencia(p.getExternalReference(), p.getMonto())
                : java.util.Optional.empty();
    p.setReconcileAttempts(p.getReconcileAttempts() + 1);
    if (found.isPresent()) {
      var result = found.get();
      PaymentRules.change(p.getEstado(), result.estado());
      boolean changed = p.getEstado() != result.estado();
      p.setProviderPaymentId(result.providerPaymentId());
      p.setEstado(result.estado());
      p.setLastProviderError(null);
      if (changed) emit(p, e);
    } else if (p.getSubmissionStartedAt() != null) p.setLastProviderError("PENDIENTE_CONCILIACION");
    repo.save(p);
  }

  @Transactional(readOnly = true)
  public List<PagoResponse> listar() {
    return (Identidad.rol("ADMIN") || Identidad.rol("AGENTE")
            ? repo.findAll()
            : repo.findByClienteId(Identidad.cliente()))
        .stream().map(this::map).toList();
  }

  @Transactional(readOnly = true)
  public PagoResponse buscar(Long id) {
    var p = entidad(id);
    if (!Identidad.rol("ADMIN") && !Identidad.rol("AGENTE"))
      Identidad.propietario(guard.context(p.getOrdenId()).clienteId());
    return map(p);
  }

  @Transactional(readOnly = true)
  public java.util.Map<String, Object> seguimiento(Long id) {
    buscar(id);
    var p = entidad(id);
    boolean external = "MERCADO_PAGO".equals(p.getProveedor());
    boolean uncertain =
        external
            && p.getProviderPaymentId() == null
            && p.getSubmissionStartedAt() != null
            && !PaymentRules.terminal(p.getEstado());
    var data = new java.util.LinkedHashMap<String, Object>();
    data.put("id", id);
    data.put("estado", p.getEstado());
    data.put("proveedor", p.getProveedor());
    data.put("pendienteConciliacion", uncertain);
    data.put("intentosConciliacion", p.getReconcileAttempts());
    data.put("ultimoResultado", p.getLastProviderError());
    data.put(
        "mensaje",
        uncertain
            ? "Esperando confirmar el resultado del proveedor; no cree otro pago"
            : "Consulte el estado de la orden para seguir la operacion");
    return data;
  }

  private Pago entidad(Long id) {
    return repo.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pago no encontrado"));
  }

  private void emit(Pago p, OperacionEvent e) {
    if (p.getEstado() == EstadoPago.APROBADO) outbox.add(e.next("PAGO_APROBADO"));
    else if (Set.of(EstadoPago.RECHAZADO, EstadoPago.CANCELADO, EstadoPago.REEMBOLSADO)
        .contains(p.getEstado()))
      outbox.add(e.next("PAGO_RECHAZADO", "Pago rechazado o cancelado"));
  }

  private ResponseStatusException conflict(String text) {
    return new ResponseStatusException(HttpStatus.CONFLICT, text);
  }

  private PagoResponse map(Pago p) {
    return new PagoResponse(
        p.getId(),
        p.getOrdenId(),
        p.getMonto(),
        p.getMoneda(),
        p.getProveedor(),
        p.getProviderPaymentId(),
        p.getIdempotencyKey(),
        p.getEstado(),
        p.getCreatedAt(),
        p.getUpdatedAt(),
        p.getMetodo(),
        p.getNroOperacion(),
        p.getComprobanteAt() != null,
        p.getMotivoRevision());
  }
}
