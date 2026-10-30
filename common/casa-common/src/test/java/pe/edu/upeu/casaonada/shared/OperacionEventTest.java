package pe.edu.upeu.casaonada.shared;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class OperacionEventTest {
  OperacionEvent e(Long propiedad, BigDecimal total) {
    return new OperacionEvent(
        "evento",
        "saga",
        "PAGO_APROBADO",
        1L,
        2L,
        propiedad,
        4L,
        new BigDecimal("500"),
        "PEN",
        "COMPRA",
        "trace",
        null,
        total);
  }

  @Test
  void validaEventoCompleto() {
    assertDoesNotThrow(() -> e(3L, new BigDecimal("10000")).validate());
  }

  @Test
  void rechazoReferenciasInvalidas() {
    assertThrows(IllegalArgumentException.class, () -> e(0L, new BigDecimal("10000")).validate());
  }

  @Test
  void rechazoAdelantoSuperiorAlTotal() {
    assertThrows(IllegalArgumentException.class, () -> e(3L, new BigDecimal("100")).validate());
  }

  @Test
  void cambioDeMensajeConservaOperacion() {
    var e = e(3L, new BigDecimal("10000"));
    assertTrue(e.sameOperation(e.next("CREAR_CONTRATO")));
    assertNotEquals(e.eventId(), e.next("CREAR_CONTRATO").eventId());
  }

  @Test
  void detectaCambioDeInmueble() {
    assertFalse(e(3L, new BigDecimal("10000")).sameOperation(e(5L, new BigDecimal("10000"))));
  }

  @Test
  void detectaCambioDePrecioTotal() {
    assertFalse(e(3L, new BigDecimal("10000")).sameOperation(e(3L, new BigDecimal("20000"))));
  }
}
