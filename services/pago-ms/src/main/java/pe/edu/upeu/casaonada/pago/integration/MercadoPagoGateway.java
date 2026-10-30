package pe.edu.upeu.casaonada.pago.integration;

import java.math.BigDecimal;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import pe.edu.upeu.casaonada.pago.domain.EstadoPago;
import pe.edu.upeu.casaonada.pago.dto.PagoRequest;

@Component
@ConditionalOnProperty(name = "app.mercadopago.enabled", havingValue = "true")
public class MercadoPagoGateway {
  private final RestClient client;

  public MercadoPagoGateway(
      @Value("${app.mercadopago.base-url:https://api.mercadopago.com}") String base,
      @Value("${app.mercadopago.access-token}") String token) {
    if (token == null || token.isBlank())
      throw new IllegalStateException("MERCADOPAGO_ACCESS_TOKEN es obligatorio");
    var factory = new SimpleClientHttpRequestFactory();
    factory.setConnectTimeout(3000);
    factory.setReadTimeout(10000);
    client =
        RestClient.builder()
            .requestFactory(factory)
            .baseUrl(base)
            .defaultHeader("Authorization", "Bearer " + token)
            .build();
  }

  @SuppressWarnings("unchecked")
  public Resultado pagar(PagoRequest r, String key, String reference) {
    if (reference == null || reference.isBlank())
      throw new IllegalArgumentException("La intencion debe persistirse antes de cobrar");
    var body = new LinkedHashMap<String, Object>();
    body.put("external_reference", reference);
    body.put("transaction_amount", r.monto());
    body.put("description", r.descripcion());
    body.put("payment_method_id", r.paymentMethodId());
    body.put("installments", r.installments() == null ? 1 : r.installments());
    body.put("payer", Map.of("email", r.payerEmail()));
    if (r.token() != null && !r.token().isBlank()) body.put("token", r.token());
    var res =
        client
            .post()
            .uri("/v1/payments")
            .header("X-Idempotency-Key", key)
            .contentType(MediaType.APPLICATION_JSON)
            .body(body)
            .retrieve()
            .body(Map.class);
    verify(res, reference, r.monto());
    return resultado(res);
  }

  @SuppressWarnings("unchecked")
  public Optional<Resultado> buscarPorReferencia(String reference, BigDecimal monto) {
    if (reference == null || reference.isBlank())
      throw new IllegalStateException("Pago sin referencia para conciliacion");
    Map<?, ?> body =
        client
            .get()
            .uri(
                b ->
                    b.path("/v1/payments/search")
                        .queryParam("external_reference", reference)
                        .queryParam("limit", 10)
                        .build())
            .retrieve()
            .body(Map.class);
    if (body == null || !(body.get("results") instanceof List<?> results))
      throw new IllegalStateException("Respuesta de conciliacion invalida");
    if (results.size() > 1)
      throw new IllegalStateException("Varias operaciones externas requieren revision financiera");
    if (results.isEmpty()) return Optional.empty();
    if (!(results.get(0) instanceof Map<?, ?> payment))
      throw new IllegalStateException("Pago de conciliacion invalido");
    verify(payment, reference, monto);
    return Optional.of(resultado(payment));
  }

  @SuppressWarnings("unchecked")
  public Resultado consultar(String id) {
    return resultado(client.get().uri("/v1/payments/{id}", id).retrieve().body(Map.class));
  }

  @SuppressWarnings("unchecked")
  public void reembolsar(String id, String key) {
    if (consultar(id).estado() == EstadoPago.REEMBOLSADO) return;
    Map<?, ?> response =
        client
            .post()
            .uri("/v1/payments/{id}/refunds", id)
            .header("X-Idempotency-Key", key)
            .contentType(MediaType.APPLICATION_JSON)
            .body(Map.of())
            .retrieve()
            .body(Map.class);
    if (response == null || !"approved".equals(Objects.toString(response.get("status"), "")))
      throw new IllegalStateException("Reembolso sin confirmacion del proveedor");
  }

  @SuppressWarnings("unchecked")
  public EstadoPago cancelar(String id, String refundKey) {
    var actual = consultar(id).estado();
    if (actual == EstadoPago.REEMBOLSADO) return actual;
    if (actual == EstadoPago.APROBADO) {
      reembolsar(id, refundKey);
      return EstadoPago.REEMBOLSADO;
    }
    if (actual == EstadoPago.CANCELADO || actual == EstadoPago.RECHAZADO)
      return EstadoPago.CANCELADO;
    var res =
        resultado(
            client
                .put()
                .uri("/v1/payments/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("status", "cancelled"))
                .retrieve()
                .body(Map.class));
    if (res.estado() != EstadoPago.CANCELADO)
      throw new IllegalStateException("Cancelacion sin confirmacion del proveedor");
    return EstadoPago.CANCELADO;
  }

  private void verify(Map<?, ?> res, String reference, BigDecimal monto) {
    if (res == null
        || !reference.equals(res.get("external_reference"))
        || !"PEN".equals(res.get("currency_id"))
        || res.get("transaction_amount") == null
        || new BigDecimal(res.get("transaction_amount").toString()).compareTo(monto) != 0)
      throw new IllegalStateException("Resultado del proveedor incompatible con la intencion");
  }

  private Resultado resultado(Map<?, ?> res) {
    if (res == null) throw new IllegalStateException("Respuesta vacia del proveedor");
    String id = Objects.toString(res.get("id"), null);
    if (id == null || id.isBlank()) throw new IllegalStateException("Pago sin identificador");
    EstadoPago estado =
        switch (Objects.toString(res.get("status"), "")) {
          case "approved" -> EstadoPago.APROBADO;
          case "rejected" -> EstadoPago.RECHAZADO;
          case "cancelled" -> EstadoPago.CANCELADO;
          case "refunded" -> EstadoPago.REEMBOLSADO;
          case "pending", "in_process", "in_mediation", "authorized" -> EstadoPago.PENDIENTE;
          default -> throw new IllegalStateException("Estado del proveedor no reconocido");
        };
    return new Resultado(id, estado);
  }

  public record Resultado(String providerPaymentId, EstadoPago estado) {}
}
