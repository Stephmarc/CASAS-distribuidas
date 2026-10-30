package pe.edu.upeu.casaonada.pago.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PaymentModeConfiguration {
  public PaymentModeConfiguration(
      @Value("${app.pagos.sandbox.enabled:false}") boolean sandbox,
      @Value("${app.mercadopago.enabled:false}") boolean external) {
    if (sandbox == external)
      throw new IllegalStateException(
          "Configure exactamente un modo de pago: SANDBOX_LOCAL o MERCADO_PAGO");
  }
}
