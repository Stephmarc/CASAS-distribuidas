package pe.edu.upeu.casaonada.reserva.integration;

import feign.Retryer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignRetryConfiguration {
  /** All Feign operations in reserva-ms are reads: at most two transport attempts. */
  @Bean
  public Retryer lecturaRetryer() {
    return new Retryer.Default(100, 300, 2);
  }
}
