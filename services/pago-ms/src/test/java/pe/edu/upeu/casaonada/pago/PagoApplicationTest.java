package pe.edu.upeu.casaonada.pago;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.ObjectProvider;
import pe.edu.upeu.casaonada.pago.domain.Pago;
import pe.edu.upeu.casaonada.pago.dto.PagoRequest;
import pe.edu.upeu.casaonada.pago.integration.MercadoPagoGateway;
import pe.edu.upeu.casaonada.pago.service.*;

class PagoApplicationTest {
  PagoService pagos;
  PagoIntentService intents;
  MercadoPagoGateway gateway;
  PagoApplication app;
  PagoRequest request;

  @BeforeEach
  @SuppressWarnings("unchecked")
  void setup() {
    pagos = mock(PagoService.class);
    intents = mock(PagoIntentService.class);
    gateway = mock(MercadoPagoGateway.class);
    var provider = (ObjectProvider<MercadoPagoGateway>) mock(ObjectProvider.class);
    when(provider.getIfAvailable()).thenReturn(gateway);
    app = new PagoApplication(pagos, intents, provider);
    request =
        new PagoRequest(1L, new BigDecimal("500"), "PEN", "Adelanto", "c@c.pe", "visa", null, 1);
    var p = new Pago();
    p.setExternalReference("casa-ref");
    when(intents.reserve(request, "key")).thenReturn(1L);
    when(intents.snapshot(1L)).thenReturn(p);
  }

  @Test
  void noContactaProveedorAntesDePersistirIntencion() {
    when(intents.claim(1L)).thenReturn(true);
    when(gateway.pagar(request, "key", "casa-ref"))
        .thenReturn(
            new MercadoPagoGateway.Resultado(
                "123", pe.edu.upeu.casaonada.pago.domain.EstadoPago.APROBADO));
    app.crear(request, "key");
    var order = inOrder(intents, gateway);
    order.verify(intents).reserve(request, "key");
    order.verify(intents).claim(1L);
    order.verify(intents).snapshot(1L);
    order.verify(gateway).pagar(request, "key", "casa-ref");
    order.verify(intents).complete(eq(1L), any());
  }

  @Test
  void reintentoNoVuelveACobrar() {
    when(intents.claim(1L)).thenReturn(false);
    app.crear(request, "key");
    verifyNoInteractions(gateway);
  }

  @Test
  void respuestaPerdidaQuedaPendienteDeConciliacion() {
    when(intents.claim(1L)).thenReturn(true);
    when(gateway.pagar(request, "key", "casa-ref"))
        .thenThrow(new org.springframework.web.client.ResourceAccessException("timeout"));
    app.crear(request, "key");
    verify(intents).failed(eq(1L), eq(false), anyString());
    verify(intents, never()).complete(any(), any());
  }

  @Test
  void huellaNoDependeDeEscalaDecimal() {
    var b =
        new PagoRequest(1L, new BigDecimal("500.00"), "PEN", "Adelanto", "c@c.pe", "visa", null, 1);
    assertEquals(PaymentFingerprint.of(request), PaymentFingerprint.of(b));
  }

  @Test
  void cambioDeMedioDePagoCambiaHuella() {
    var b =
        new PagoRequest(1L, new BigDecimal("500"), "PEN", "Adelanto", "c@c.pe", "master", null, 1);
    assertNotEquals(PaymentFingerprint.of(request), PaymentFingerprint.of(b));
  }
}
