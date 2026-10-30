package pe.edu.upeu.casaonada.pago.service;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import pe.edu.upeu.casaonada.pago.dto.*;
import pe.edu.upeu.casaonada.pago.integration.MercadoPagoGateway;

@Service
public class PagoApplication {
  private final PagoService pagos;
  private final PagoIntentService intents;
  private final ObjectProvider<MercadoPagoGateway> provider;

  public PagoApplication(PagoService p, PagoIntentService i, ObjectProvider<MercadoPagoGateway> g) {
    pagos = p;
    intents = i;
    provider = g;
  }

  public PagoResponse crear(PagoRequest request, String key) {
    var gateway = provider.getIfAvailable();
    if (gateway == null) return pagos.crear(request, key);
    Long id = intents.reserve(request, key);
    if (intents.claim(id)) {
      var snapshot = intents.snapshot(id);
      MercadoPagoGateway.Resultado result = null;
      try {
        result = gateway.pagar(request, key, snapshot.getExternalReference());
      } catch (RestClientResponseException ex) {
        int s = ex.getStatusCode().value();
        intents.failed(id, s >= 400 && s < 500 && s != 408 && s != 409 && s != 429, "HTTP_" + s);
      } catch (RuntimeException ex) {
        intents.failed(id, false, ex.getClass().getSimpleName());
      }
      if (result != null) intents.complete(id, result);
    }
    return pagos.buscar(id);
  }
}
