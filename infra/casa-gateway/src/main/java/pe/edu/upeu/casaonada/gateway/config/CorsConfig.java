package pe.edu.upeu.casaonada.gateway.config;

import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.web.cors.*;

@Configuration
public class CorsConfig {
  @Bean
  CorsConfigurationSource corsConfigurationSource(
      @Value("${app.cors.allowed-origins:http://localhost:4300}")
          String origins) {
    var c = new CorsConfiguration();
    c.setAllowedOrigins(
        Arrays.stream(origins.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList());
    c.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    c.setAllowedHeaders(
        List.of("Authorization", "Content-Type", "X-Idempotency-Key", "X-Trace-ID"));
    c.setExposedHeaders(List.of("Location", "X-Trace-ID"));
    c.setAllowCredentials(false);
    c.setMaxAge(3600L);
    var source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/api/**", c);
    return source;
  }
}
