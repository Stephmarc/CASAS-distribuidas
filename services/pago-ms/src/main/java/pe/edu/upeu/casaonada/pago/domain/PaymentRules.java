package pe.edu.upeu.casaonada.pago.domain;

public final class PaymentRules {
  private PaymentRules() {}

  public enum Recovery {
    NEVER_SUBMITTED,
    SEARCH_REFERENCE,
    QUERY_PAYMENT
  }

  public static Recovery recovery(String providerId, boolean started) {
    return providerId != null && !providerId.isBlank()
        ? Recovery.QUERY_PAYMENT
        : started ? Recovery.SEARCH_REFERENCE : Recovery.NEVER_SUBMITTED;
  }

  public static boolean terminal(EstadoPago state) {
    return state == EstadoPago.RECHAZADO
        || state == EstadoPago.CANCELADO
        || state == EstadoPago.REEMBOLSADO;
  }

  public static void change(EstadoPago from, EstadoPago to) {
    if (from == to) return;
    if (terminal(from) || from == EstadoPago.APROBADO && to != EstadoPago.REEMBOLSADO)
      throw new IllegalStateException("Respuesta atrasada del proveedor no puede revertir el pago");
  }
}
