package pe.edu.upeu.casaonada.shared;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class DomainRulesTest {
  @Test
  void aceptaImportesValidos() {
    assertDoesNotThrow(() -> DomainRules.money(new BigDecimal("500.00"), "Adelanto"));
  }

  @Test
  void rechazaCeroYNegativos() {
    for (String s : new String[] {"0", "-1"})
      assertThrows(
          IllegalArgumentException.class, () -> DomainRules.money(new BigDecimal(s), "Adelanto"));
  }

  @Test
  void rechazaPrecisionQueLaBaseRedondearia() {
    assertThrows(
        IllegalArgumentException.class,
        () -> DomainRules.money(new BigDecimal("100.001"), "Adelanto"));
  }

  @Test
  void rechazaDesborde() {
    assertThrows(
        IllegalArgumentException.class,
        () -> DomainRules.money(new BigDecimal("1000000000000"), "Adelanto"));
  }

  @Test
  void permiteSoloEstadosAdministrativosDeCatalogo() {
    for (String s : new String[] {"BORRADOR", "PUBLICADA", "DISPONIBLE", "INACTIVA"})
      assertDoesNotThrow(() -> DomainRules.catalogState(s));
  }

  @Test
  void ventaYBloqueoExigenSaga() {
    for (String s : new String[] {"RESERVADA", "VENDIDA", "ALQUILADA", "EN_PROCESO"})
      assertThrows(IllegalArgumentException.class, () -> DomainRules.catalogState(s));
  }

  @Test
  void visitaTerminadaNoSeReabre() {
    for (String s : new String[] {"REALIZADA", "NO_ASISTIO", "CANCELADA"})
      assertThrows(
          IllegalArgumentException.class, () -> DomainRules.transitionVisit(s, "REPROGRAMADA"));
  }

  @Test
  void visitaSolicitadaNoSaltaARealizada() {
    assertThrows(
        IllegalArgumentException.class,
        () -> DomainRules.transitionVisit("SOLICITADA", "REALIZADA"));
  }

  @Test
  void visitaConfirmadaPuedeCancelarse() {
    assertDoesNotThrow(() -> DomainRules.transitionVisit("CONFIRMADA", "CANCELADA"));
  }
}
