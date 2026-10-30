package pe.edu.upeu.casaonada.pago;

import static org.junit.jupiter.api.Assertions.*;

import com.sun.net.httpserver.*;
import java.math.BigDecimal;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.*;
import pe.edu.upeu.casaonada.pago.domain.EstadoPago;
import pe.edu.upeu.casaonada.pago.dto.PagoRequest;
import pe.edu.upeu.casaonada.pago.integration.MercadoPagoGateway;

class MercadoPagoGatewayTest {
  HttpServer server;
  MercadoPagoGateway gateway;
  AtomicReference<String> response;
  AtomicReference<String> requestBody;
  AtomicReference<String> key;
  int status;

  String payment(String amount, String ref, String state) {
    return "{\"id\":123,\"external_reference\":\""
        + ref
        + "\",\"currency_id\":\"PEN\",\"transaction_amount\":"
        + amount
        + ",\"status\":\""
        + state
        + "\"}";
  }

  @BeforeEach
  void setup() throws Exception {
    response = new AtomicReference<>();
    requestBody = new AtomicReference<>();
    key = new AtomicReference<>();
    status = 200;
    server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    server.createContext(
        "/",
        x -> {
          requestBody.set(new String(x.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
          key.set(x.getRequestHeaders().getFirst("X-Idempotency-Key"));
          byte[] b = response.get().getBytes(StandardCharsets.UTF_8);
          x.getResponseHeaders().add("Content-Type", "application/json");
          x.sendResponseHeaders(status, b.length);
          x.getResponseBody().write(b);
          x.close();
        });
    server.start();
    gateway =
        new MercadoPagoGateway("http://127.0.0.1:" + server.getAddress().getPort(), "TEST-local");
  }

  @AfterEach
  void stop() {
    server.stop(0);
  }

  @Test
  void enviaReferenciaYClavePersistidas() {
    response.set(payment("500", "casa-ref", "approved"));
    var r =
        new PagoRequest(1L, new BigDecimal("500"), "PEN", "Adelanto", "c@c.pe", "visa", null, 1);
    assertEquals(EstadoPago.APROBADO, gateway.pagar(r, "idem", "casa-ref").estado());
    assertTrue(requestBody.get().contains("casa-ref"));
    assertEquals("idem", key.get());
  }

  @Test
  void noDaPorFallidoUnCobroAusenteDeLaBusqueda() {
    response.set("{\"results\":[]}");
    assertTrue(gateway.buscarPorReferencia("casa-ref", new BigDecimal("500")).isEmpty());
  }

  @Test
  void recuperaRespuestaPerdidaPorReferencia() {
    response.set("{\"results\":[" + payment("500", "casa-ref", "approved") + "]}");
    assertEquals(
        "123",
        gateway
            .buscarPorReferencia("casa-ref", new BigDecimal("500"))
            .orElseThrow()
            .providerPaymentId());
  }

  @Test
  void noAceptaImporteDelProveedorDistinto() {
    response.set("{\"results\":[" + payment("1", "casa-ref", "approved") + "]}");
    assertThrows(
        IllegalStateException.class,
        () -> gateway.buscarPorReferencia("casa-ref", new BigDecimal("500")));
  }

  @Test
  void noAceptaReferenciaDelProveedorDistinta() {
    response.set("{\"results\":[" + payment("500", "otra", "approved") + "]}");
    assertThrows(
        IllegalStateException.class,
        () -> gateway.buscarPorReferencia("casa-ref", new BigDecimal("500")));
  }

  @Test
  void variasCoincidenciasRequierenRevision() {
    response.set(
        "{\"results\":["
            + payment("500", "casa-ref", "approved")
            + ","
            + payment("500", "casa-ref", "approved")
            + "]}");
    assertThrows(
        IllegalStateException.class,
        () -> gateway.buscarPorReferencia("casa-ref", new BigDecimal("500")));
  }

  @Test
  void rechazaEstadoDesconocido() {
    response.set(payment("500", "casa-ref", "nuevo_estado"));
    assertThrows(IllegalStateException.class, () -> gateway.consultar("123"));
  }

  @Test
  void errorDelProveedorNoSeConvierteEnAprobacion() {
    status = 503;
    response.set("{}");
    assertThrows(
        org.springframework.web.client.RestClientResponseException.class,
        () -> gateway.consultar("123"));
  }
}
