package pe.edu.upeu.casaonada.shared;

import java.math.BigDecimal;
import java.util.Set;

public final class DomainRules {
  private DomainRules() {}

  public static String requiredState(java.util.Map<String, String> body) {
    String state = body == null ? null : body.get("estado");
    if (state == null || state.isBlank())
      throw new IllegalArgumentException("estado es obligatorio");
    return state;
  }

  public static void money(BigDecimal value, String name) {
    if (value == null
        || value.signum() <= 0
        || value.stripTrailingZeros().scale() > 2
        || value.compareTo(new BigDecimal("999999999999.99")) > 0)
      throw new IllegalArgumentException(name + " debe ser positivo con hasta dos decimales");
  }

  public static void catalogState(String state) {
    if (!Set.of("BORRADOR", "PUBLICADA", "DISPONIBLE", "INACTIVA").contains(state))
      throw new IllegalArgumentException(
          "Los estados de reserva, venta y alquiler se administran mediante la SAGA");
  }

  public static void transitionVisit(String from, String to) {
    boolean valid =
        from.equals(to)
            || switch (from) {
              case "SOLICITADA" -> Set.of("CONFIRMADA", "REPROGRAMADA", "CANCELADA").contains(to);
              case "CONFIRMADA", "REPROGRAMADA" ->
                  Set.of("CONFIRMADA", "REPROGRAMADA", "REALIZADA", "CANCELADA", "NO_ASISTIO")
                      .contains(to);
              default -> false;
            };
    if (!valid) throw new IllegalArgumentException("Transicion de visita no permitida");
  }
}
