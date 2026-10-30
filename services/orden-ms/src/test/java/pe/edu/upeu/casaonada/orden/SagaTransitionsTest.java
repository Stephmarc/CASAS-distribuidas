package pe.edu.upeu.casaonada.orden;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import pe.edu.upeu.casaonada.orden.domain.*;

class SagaTransitionsTest {
  @Test
  void compraCompletaSoloConPagoAprobado() {
    var s = EstadoOrden.VALIDANDO_RESERVA;
    String[] events = {
      "RESERVA_ASOCIADA",
      "PROPIEDAD_BLOQUEADA",
      "PAGO_APROBADO",
      "CONTRATO_GENERADO",
      "PROPIEDAD_CONFIRMADA",
      "RESERVA_CONVERTIDA"
    };
    for (var e : events) s = SagaTransitions.apply(s, e).estado();
    assertEquals(EstadoOrden.CONFIRMADA, s);
  }

  @Test
  void noConfirmaSinPago() {
    assertEquals(
        EstadoOrden.ESPERANDO_PAGO,
        SagaTransitions.apply(EstadoOrden.ESPERANDO_PAGO, "CONTRATO_GENERADO").estado());
  }

  @Test
  void rechazoCompensaTodosLosPasos() {
    var change = SagaTransitions.apply(EstadoOrden.ESPERANDO_PAGO, "PAGO_RECHAZADO");
    assertEquals("CANCELAR_CONTRATO", change.command());
    var s = change.estado();
    for (var e :
        new String[] {
          "CONTRATO_CANCELADO", "PAGO_REEMBOLSADO", "PROPIEDAD_LIBERADA", "RESERVA_CANCELADA"
        }) s = SagaTransitions.apply(s, e).estado();
    assertEquals(EstadoOrden.CANCELADA, s);
  }

  @Test
  void falloContratoReembolsaAntesDeLiberar() {
    var c = SagaTransitions.apply(EstadoOrden.GENERANDO_CONTRATO, "CONTRATO_RECHAZADO");
    c = SagaTransitions.apply(c.estado(), "CONTRATO_CANCELADO");
    assertEquals("REEMBOLSAR_PAGO", c.command());
    assertEquals(
        EstadoOrden.REEMBOLSANDO_PAGO,
        SagaTransitions.apply(c.estado(), "PROPIEDAD_LIBERADA").estado());
  }

  @Test
  void timeoutEnCadaEtapaIniciaCompensacion() {
    for (var s : EstadoOrden.values())
      if (!s.terminal() && !s.compensando())
        assertEquals(EstadoOrden.CANCELANDO_CONTRATO, SagaTransitions.apply(s, "TIMEOUT").estado());
  }

  @Test
  void pagoTardioNoResucitaSaga() {
    for (var s : EstadoOrden.values())
      if (s.compensando() || s == EstadoOrden.CANCELADA)
        assertEquals(s, SagaTransitions.apply(s, "PAGO_APROBADO").estado());
  }

  @Test
  void duplicadosNoRepitenComando() {
    assertNull(SagaTransitions.apply(EstadoOrden.GENERANDO_CONTRATO, "PAGO_APROBADO").command());
    assertNull(SagaTransitions.apply(EstadoOrden.CONFIRMADA, "RESERVA_CONVERTIDA").command());
  }

  @Test
  void terminalesNoCambian() {
    for (var s : new EstadoOrden[] {EstadoOrden.CONFIRMADA, EstadoOrden.CANCELADA})
      for (var e : new String[] {"CANCELAR", "TIMEOUT", "PAGO_APROBADO", "PAGO_RECHAZADO"})
        assertEquals(s, SagaTransitions.apply(s, e).estado());
  }

  @Test
  void cancelarDuranteConfirmacionCompensa() {
    assertEquals(
        "CANCELAR_CONTRATO",
        SagaTransitions.apply(EstadoOrden.CONVIRTIENDO_RESERVA, "CANCELAR").command());
  }

  @Test
  void canceladaSoloTrasUltimaCompensacion() {
    assertEquals(
        "ORDEN_CANCELADA",
        SagaTransitions.apply(EstadoOrden.CANCELANDO_RESERVA, "RESERVA_CANCELADA").command());
  }

  @Test
  void rechazoAtrasadoDeReservaNoRevierteEtapaPosterior() {
    assertEquals(
        EstadoOrden.ESPERANDO_PAGO,
        SagaTransitions.apply(EstadoOrden.ESPERANDO_PAGO, "RESERVA_RECHAZADA").estado());
  }

  @Test
  void rechazoDePropiedadEnConfirmacionSiCompensa() {
    assertEquals(
        EstadoOrden.CANCELANDO_CONTRATO,
        SagaTransitions.apply(EstadoOrden.CONFIRMANDO_PROPIEDAD, "PROPIEDAD_RECHAZADA").estado());
  }
}
