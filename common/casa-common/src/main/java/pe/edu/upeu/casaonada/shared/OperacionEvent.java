package pe.edu.upeu.casaonada.shared;

import java.math.BigDecimal;
import java.util.UUID;

public record OperacionEvent(
    String eventId,
    String sagaId,
    String type,
    Long ordenId,
    Long reservaId,
    Long propiedadId,
    Long clienteId,
    BigDecimal monto,
    String moneda,
    String tipo,
    String traceId,
    String motivo,
    BigDecimal total) {
  public void validate() {
    if (eventId == null
        || !eventId.matches("[A-Za-z0-9_-]{1,36}")
        || sagaId == null
        || !sagaId.matches("[A-Za-z0-9_-]{1,36}")
        || type == null
        || !type.matches("[A-Z_]{1,60}"))
      throw new IllegalArgumentException("Identificadores de evento invalidos");
    if (ordenId == null
        || ordenId <= 0
        || reservaId == null
        || reservaId <= 0
        || propiedadId == null
        || propiedadId <= 0
        || clienteId == null
        || clienteId <= 0) throw new IllegalArgumentException("Referencias de evento invalidas");
    DomainRules.money(monto, "Adelanto");
    DomainRules.money(total, "Total");
    if (total.compareTo(monto) < 0
        || !"PEN".equals(moneda)
        || !("COMPRA".equals(tipo) || "ALQUILER".equals(tipo))
        || traceId != null && traceId.length() > 80
        || motivo != null && motivo.length() > 1000)
      throw new IllegalArgumentException("Datos de evento invalidos");
  }

  public boolean sameOperation(OperacionEvent other) {
    return java.util.Objects.equals(sagaId, other.sagaId)
        && java.util.Objects.equals(ordenId, other.ordenId)
        && java.util.Objects.equals(reservaId, other.reservaId)
        && java.util.Objects.equals(propiedadId, other.propiedadId)
        && java.util.Objects.equals(clienteId, other.clienteId)
        && monto.compareTo(other.monto) == 0
        && total.compareTo(other.total) == 0
        && java.util.Objects.equals(moneda, other.moneda)
        && java.util.Objects.equals(tipo, other.tipo);
  }

  public OperacionEvent next(String newType) {
    return next(newType, motivo);
  }

  public OperacionEvent next(String newType, String detail) {
    return new OperacionEvent(
        UUID.randomUUID().toString(),
        sagaId,
        newType,
        ordenId,
        reservaId,
        propiedadId,
        clienteId,
        monto,
        moneda,
        tipo,
        traceId,
        detail,
        total);
  }
}
