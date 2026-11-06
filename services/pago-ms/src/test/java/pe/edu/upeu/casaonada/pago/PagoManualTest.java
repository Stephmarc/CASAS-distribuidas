package pe.edu.upeu.casaonada.pago;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;
import pe.edu.upeu.casaonada.pago.domain.*;
import pe.edu.upeu.casaonada.pago.integration.MercadoPagoGateway;
import pe.edu.upeu.casaonada.pago.repository.PagoComprobanteRepository;
import pe.edu.upeu.casaonada.pago.repository.PagoRepository;
import pe.edu.upeu.casaonada.pago.service.*;
import pe.edu.upeu.casaonada.shared.*;

/** Pago manual: el cliente sube la captura y un agente la aprueba, observa o rechaza. */
class PagoManualTest {
  PagoRepository repo;
  PagoComprobanteRepository comprobantes;
  PagoSagaGuard guard;
  Outbox outbox;
  PagoService service;
  Pago pago;
  static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', 1, 2, 3};

  @BeforeEach
  @SuppressWarnings("unchecked")
  void setup() {
    repo = mock(PagoRepository.class);
    comprobantes = mock(PagoComprobanteRepository.class);
    guard = mock(PagoSagaGuard.class);
    outbox = mock(Outbox.class);
    service =
        new PagoService(
            repo,
            (ObjectProvider<MercadoPagoGateway>) mock(ObjectProvider.class),
            mock(ConsultasRemotas.class),
            guard,
            outbox,
            true,
            mock(jakarta.persistence.EntityManager.class),
            mock(IdempotencyLocks.class),
            comprobantes);
    var event =
        new OperacionEvent("event", "saga", "PAGO", 1L, 1L, 1L, 1L, new BigDecimal("500"), "PEN", "COMPRA", "trace", null, new BigDecimal("100000"));
    pago = new Pago();
    pago.setOrdenId(1L);
    pago.setClienteId(1L);
    pago.setProveedor("SANDBOX_LOCAL");
    pago.setMonto(new BigDecimal("500"));
    pago.setMoneda("PEN");
    pago.setEstado(EstadoPago.PENDIENTE);
    when(repo.findById(1L)).thenReturn(Optional.of(pago));
    when(guard.context(1L)).thenReturn(event);
    when(comprobantes.findById(1L)).thenReturn(Optional.empty());
    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken("agente", "", List.of(new SimpleGrantedAuthority("ROLE_AGENTE"))));
  }

  @AfterEach
  void clear() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void elComprobanteQuedaRegistradoConMetodoYNumero() {
    var r = service.comprobante(1L, "YAPE", "OP-12345", "image/png", PNG);
    assertEquals("YAPE", pago.getMetodo());
    assertEquals("OP-12345", pago.getNroOperacion());
    assertNotNull(pago.getComprobanteAt());
    assertTrue(r.comprobante());
    verify(comprobantes).save(any());
  }

  @Test
  void rechazaMetodoDePagoDesconocido() {
    assertThrows(IllegalArgumentException.class, () -> service.comprobante(1L, "BITCOIN", "OP-12345", "image/png", PNG));
  }

  @Test
  void rechazaNumeroDeOperacionInvalido() {
    assertThrows(IllegalArgumentException.class, () -> service.comprobante(1L, "PLIN", "x", "image/png", PNG));
  }

  @Test
  void soloAceptaImagenes() {
    assertThrows(IllegalArgumentException.class, () -> service.comprobante(1L, "YAPE", "OP-12345", "application/pdf", PNG));
  }

  @Test
  void rechazaCapturasMayoresA3Mb() {
    assertThrows(IllegalArgumentException.class, () -> service.comprobante(1L, "YAPE", "OP-12345", "image/png", new byte[3 * 1024 * 1024 + 1]));
  }

  @Test
  void noSeEnviaCapturaDeUnPagoYaResuelto() {
    pago.setEstado(EstadoPago.APROBADO);
    var e = assertThrows(ResponseStatusException.class, () -> service.comprobante(1L, "YAPE", "OP-12345", "image/png", PNG));
    assertEquals(HttpStatus.CONFLICT, e.getStatusCode());
  }

  @Test
  void noSeApruebaSinCaptura() {
    var e = assertThrows(ResponseStatusException.class, () -> service.revisar(1L, "APROBAR", null));
    assertEquals(HttpStatus.CONFLICT, e.getStatusCode());
    assertEquals(EstadoPago.PENDIENTE, pago.getEstado());
    verify(outbox, never()).add(any());
  }

  @Test
  void elAgenteApruebaConCapturaYAvanzaLaSaga() {
    pago.setComprobanteAt(OffsetDateTime.now());
    service.revisar(1L, "APROBAR", null);
    assertEquals(EstadoPago.APROBADO, pago.getEstado());
    verify(outbox).add(any());
  }

  @Test
  void observarPideOtraCapturaSinCancelarLaOperacion() {
    pago.setComprobanteAt(OffsetDateTime.now());
    service.revisar(1L, "OBSERVAR", "No se ve el numero de operacion");
    assertEquals(EstadoPago.PENDIENTE, pago.getEstado());
    assertNull(pago.getComprobanteAt());
    assertEquals("No se ve el numero de operacion", pago.getMotivoRevision());
    verify(comprobantes).deleteById(1L);
    verify(outbox, never()).add(any());
  }

  @Test
  void observarYRechazarExigenMotivo() {
    pago.setComprobanteAt(OffsetDateTime.now());
    assertThrows(IllegalArgumentException.class, () -> service.revisar(1L, "OBSERVAR", " "));
    assertThrows(IllegalArgumentException.class, () -> service.revisar(1L, "RECHAZAR", null));
  }

  @Test
  void rechazarCancelaLaOperacion() {
    pago.setComprobanteAt(OffsetDateTime.now());
    service.revisar(1L, "RECHAZAR", "Monto incorrecto");
    assertEquals(EstadoPago.RECHAZADO, pago.getEstado());
    verify(outbox).add(any());
  }

  @Test
  void unPagoResueltoNoSeVuelveARevisar() {
    pago.setComprobanteAt(OffsetDateTime.now());
    pago.setEstado(EstadoPago.APROBADO);
    var e = assertThrows(ResponseStatusException.class, () -> service.revisar(1L, "RECHAZAR", "tarde"));
    assertEquals(HttpStatus.CONFLICT, e.getStatusCode());
  }

  @Test
  void rechazaAccionesDesconocidas() {
    pago.setComprobanteAt(OffsetDateTime.now());
    assertThrows(IllegalArgumentException.class, () -> service.revisar(1L, "BORRAR", "x"));
  }
}
