package pe.edu.upeu.casaonada.pago.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "pagos")
public class Pago {
  @Column(name = "cliente_id")
  private Long clienteId;

  @Column(name = "external_reference", length = 80, unique = true)
  private String externalReference;

  @Column(name = "request_hash", length = 64)
  private String requestHash;

  @Column(name = "submission_started_at")
  private OffsetDateTime submissionStartedAt;

  @Column(name = "last_provider_error", length = 120)
  private String lastProviderError;

  @Column(name = "reconcile_attempts", nullable = false)
  private int reconcileAttempts;

  public Long getClienteId() {
    return clienteId;
  }

  public void setClienteId(Long v) {
    clienteId = v;
  }

  public String getExternalReference() {
    return externalReference;
  }

  public void setExternalReference(String v) {
    externalReference = v;
  }

  public String getRequestHash() {
    return requestHash;
  }

  public void setRequestHash(String v) {
    requestHash = v;
  }

  public OffsetDateTime getSubmissionStartedAt() {
    return submissionStartedAt;
  }

  public void setSubmissionStartedAt(OffsetDateTime v) {
    submissionStartedAt = v;
  }

  public String getLastProviderError() {
    return lastProviderError;
  }

  public void setLastProviderError(String v) {
    lastProviderError = v;
  }

  public int getReconcileAttempts() {
    return reconcileAttempts;
  }

  public void setReconcileAttempts(int v) {
    reconcileAttempts = v;
  }

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "orden_id", nullable = false)
  private Long ordenId;

  @Column(nullable = false, precision = 14, scale = 2)
  private BigDecimal monto;

  @Column(nullable = false, length = 8)
  private String moneda;

  @Column(nullable = false, length = 40)
  private String proveedor;

  @Column(name = "provider_payment_id", length = 120)
  private String providerPaymentId;

  @Column(name = "idempotency_key", nullable = false, length = 120, unique = true)
  private String idempotencyKey;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private EstadoPago estado;

  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private OffsetDateTime updatedAt;

  @PrePersist
  void p() {
    var n = OffsetDateTime.now();
    createdAt = n;
    updatedAt = n;
  }

  @PreUpdate
  void u() {
    updatedAt = OffsetDateTime.now();
  }

  public Long getId() {
    return id;
  }

  public Long getOrdenId() {
    return ordenId;
  }

  public void setOrdenId(Long v) {
    ordenId = v;
  }

  public BigDecimal getMonto() {
    return monto;
  }

  public void setMonto(BigDecimal v) {
    monto = v;
  }

  public String getMoneda() {
    return moneda;
  }

  public void setMoneda(String v) {
    moneda = v;
  }

  public String getProveedor() {
    return proveedor;
  }

  public void setProveedor(String v) {
    proveedor = v;
  }

  public String getProviderPaymentId() {
    return providerPaymentId;
  }

  public void setProviderPaymentId(String v) {
    providerPaymentId = v;
  }

  public String getIdempotencyKey() {
    return idempotencyKey;
  }

  public void setIdempotencyKey(String v) {
    idempotencyKey = v;
  }

  public EstadoPago getEstado() {
    return estado;
  }

  public void setEstado(EstadoPago v) {
    estado = v;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  public OffsetDateTime getUpdatedAt() {
    return updatedAt;
  }
}
