package pe.edu.upeu.casaonada.pago;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import pe.edu.upeu.casaonada.pago.domain.*;

class PaymentRulesTest {
  @Test
  void intencionNoEnviadaPuedeCancelarseSinCobro() {
    assertEquals(PaymentRules.Recovery.NEVER_SUBMITTED, PaymentRules.recovery(null, false));
  }

  @Test
  void respuestaPerdidaDebeConciliarse() {
    assertEquals(PaymentRules.Recovery.SEARCH_REFERENCE, PaymentRules.recovery(null, true));
  }

  @Test
  void pagoConIdentificadorSeConsulta() {
    assertEquals(PaymentRules.Recovery.QUERY_PAYMENT, PaymentRules.recovery("123", true));
  }

  @Test
  void reembolsoNoRegresaAAprobado() {
    assertThrows(
        IllegalStateException.class,
        () -> PaymentRules.change(EstadoPago.REEMBOLSADO, EstadoPago.APROBADO));
  }

  @Test
  void cancelacionNoRegresaAPendiente() {
    assertThrows(
        IllegalStateException.class,
        () -> PaymentRules.change(EstadoPago.CANCELADO, EstadoPago.PENDIENTE));
  }

  @Test
  void aprobadoSoloSeRevierteConReembolso() {
    assertThrows(
        IllegalStateException.class,
        () -> PaymentRules.change(EstadoPago.APROBADO, EstadoPago.RECHAZADO));
    assertDoesNotThrow(() -> PaymentRules.change(EstadoPago.APROBADO, EstadoPago.REEMBOLSADO));
  }

  @Test
  void respuestaDuplicadaEsValida() {
    assertDoesNotThrow(() -> PaymentRules.change(EstadoPago.APROBADO, EstadoPago.APROBADO));
  }
}
