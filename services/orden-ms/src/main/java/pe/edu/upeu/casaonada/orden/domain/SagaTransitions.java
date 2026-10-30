package pe.edu.upeu.casaonada.orden.domain;

public final class SagaTransitions {
  public record Transition(EstadoOrden estado, String command) {}

  private SagaTransitions() {}

  public static String command(EstadoOrden s) {
    return switch (s) {
      case VALIDANDO_RESERVA -> "ASOCIAR_RESERVA";
      case BLOQUEANDO -> "BLOQUEAR_PROPIEDAD";
      case GENERANDO_CONTRATO -> "CREAR_CONTRATO";
      case CONFIRMANDO_PROPIEDAD -> "CONFIRMAR_PROPIEDAD";
      case CONVIRTIENDO_RESERVA -> "CONVERTIR_RESERVA";
      case CANCELANDO_CONTRATO -> "CANCELAR_CONTRATO";
      case REEMBOLSANDO_PAGO -> "REEMBOLSAR_PAGO";
      case LIBERANDO_PROPIEDAD -> "LIBERAR_PROPIEDAD";
      case CANCELANDO_RESERVA -> "CANCELAR_RESERVA";
      default -> null;
    };
  }

  public static Transition apply(EstadoOrden s, String event) {
    if (s.terminal()) return new Transition(s, null);
    if (!s.compensando()
        && (event.equals("TIMEOUT") || event.equals("CANCELAR") || failureMatches(s, event)))
      return move(EstadoOrden.CANCELANDO_CONTRATO);
    EstadoOrden next =
        switch (s) {
          case VALIDANDO_RESERVA -> event.equals("RESERVA_ASOCIADA") ? EstadoOrden.BLOQUEANDO : s;
          case BLOQUEANDO -> event.equals("PROPIEDAD_BLOQUEADA") ? EstadoOrden.ESPERANDO_PAGO : s;
          case ESPERANDO_PAGO -> event.equals("PAGO_APROBADO") ? EstadoOrden.GENERANDO_CONTRATO : s;
          case GENERANDO_CONTRATO ->
              event.equals("CONTRATO_GENERADO") ? EstadoOrden.CONFIRMANDO_PROPIEDAD : s;
          case CONFIRMANDO_PROPIEDAD ->
              event.equals("PROPIEDAD_CONFIRMADA") ? EstadoOrden.CONVIRTIENDO_RESERVA : s;
          case CONVIRTIENDO_RESERVA ->
              event.equals("RESERVA_CONVERTIDA") ? EstadoOrden.CONFIRMADA : s;
          case CANCELANDO_CONTRATO ->
              event.equals("CONTRATO_CANCELADO") ? EstadoOrden.REEMBOLSANDO_PAGO : s;
          case REEMBOLSANDO_PAGO ->
              event.equals("PAGO_REEMBOLSADO") ? EstadoOrden.LIBERANDO_PROPIEDAD : s;
          case LIBERANDO_PROPIEDAD ->
              event.equals("PROPIEDAD_LIBERADA") ? EstadoOrden.CANCELANDO_RESERVA : s;
          case CANCELANDO_RESERVA -> event.equals("RESERVA_CANCELADA") ? EstadoOrden.CANCELADA : s;
          default -> s;
        };
    return next == s ? new Transition(s, null) : move(next);
  }

  private static boolean failureMatches(EstadoOrden s, String event) {
    return switch (event) {
      case "RESERVA_RECHAZADA" ->
          s == EstadoOrden.VALIDANDO_RESERVA || s == EstadoOrden.CONVIRTIENDO_RESERVA;
      case "PROPIEDAD_RECHAZADA" ->
          s == EstadoOrden.BLOQUEANDO || s == EstadoOrden.CONFIRMANDO_PROPIEDAD;
      case "CONTRATO_RECHAZADO" -> s == EstadoOrden.GENERANDO_CONTRATO;
      case "PAGO_RECHAZADO" ->
          s == EstadoOrden.ESPERANDO_PAGO
              || s == EstadoOrden.GENERANDO_CONTRATO
              || s == EstadoOrden.CONFIRMANDO_PROPIEDAD
              || s == EstadoOrden.CONVIRTIENDO_RESERVA;
      default -> false;
    };
  }

  private static Transition move(EstadoOrden s) {
    return new Transition(
        s,
        s == EstadoOrden.CONFIRMADA
            ? "ORDEN_CONFIRMADA"
            : s == EstadoOrden.CANCELADA ? "ORDEN_CANCELADA" : command(s));
  }
}
