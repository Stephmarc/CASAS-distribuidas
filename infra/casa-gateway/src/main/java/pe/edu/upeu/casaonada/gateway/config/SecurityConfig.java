package pe.edu.upeu.casaonada.gateway.config;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http.cors(org.springframework.security.config.Customizer.withDefaults())
        .csrf(csrf -> csrf.disable())
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            auth ->
                auth.dispatcherTypeMatchers(jakarta.servlet.DispatcherType.ERROR).permitAll().requestMatchers("/actuator/health", "/actuator/health/**", "/actuator/info", "/actuator/prometheus")
                    .permitAll()
                    .requestMatchers("/api/v1/auth/me")
                    .authenticated()
                    .requestMatchers(HttpMethod.GET, "/api/v1/propiedades/**")
                    .permitAll()
                    .requestMatchers("/api/v1/propiedades/**")
                    .hasAnyRole("AGENTE", "ADMIN")
                    .requestMatchers(HttpMethod.GET, "/api/v1/agentes/**")
                    .hasAnyRole("CLIENTE", "AGENTE", "ADMIN")
                    .requestMatchers("/api/v1/agentes/**")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.POST, "/api/v1/visitas/*/cancelar")
                    .hasAnyRole("CLIENTE", "ADMIN")
                    .requestMatchers(HttpMethod.POST, "/api/v1/visitas/**")
                    .hasAnyRole("CLIENTE", "ADMIN")
                    .requestMatchers(HttpMethod.GET, "/api/v1/visitas/**")
                    .hasAnyRole("CLIENTE", "AGENTE", "ADMIN")
                    .requestMatchers("/api/v1/visitas/**")
                    .hasAnyRole("AGENTE", "ADMIN")
                    .requestMatchers(HttpMethod.GET, "/api/v1/reservas/**")
                    .hasAnyRole("CLIENTE", "AGENTE", "ADMIN")
                    .requestMatchers(HttpMethod.POST, "/api/v1/reservas/**")
                    .hasAnyRole("CLIENTE", "ADMIN")
                    .requestMatchers(HttpMethod.PATCH, "/api/v1/reservas/**")
                    .hasAnyRole("AGENTE", "ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/api/v1/reservas/**")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.POST, "/api/v1/pagos/**")
                    .hasAnyRole("CLIENTE", "ADMIN")
                    .requestMatchers(HttpMethod.GET, "/api/v1/pagos/**")
                    .hasAnyRole("CLIENTE", "AGENTE", "ADMIN")
                    .requestMatchers(HttpMethod.POST, "/api/v1/contratos/*/aceptar")
                    .hasAnyRole("CLIENTE", "ADMIN")
                    .requestMatchers(HttpMethod.GET, "/api/v1/contratos/**")
                    .hasAnyRole("CLIENTE", "AGENTE", "ADMIN")
                    .requestMatchers("/api/v1/contratos/**")
                    .hasAnyRole("AGENTE", "ADMIN")
                    .requestMatchers("/api/v1/operaciones/**")
                    .hasRole("ADMIN")
                    .requestMatchers(
                        HttpMethod.GET,
                        "/api/v1/clientes/**",
                        "/api/v1/ordenes/**",
                        "/api/v1/notificaciones/**")
                    .hasAnyRole("CLIENTE", "AGENTE", "ADMIN")
                    .requestMatchers(
                        HttpMethod.POST, "/api/v1/ordenes/**", "/api/v1/notificaciones/*/leer")
                    .hasAnyRole("CLIENTE", "ADMIN")
                    .requestMatchers("/api/v1/clientes/**", "/api/v1/notificaciones/**")
                    .hasRole("ADMIN")
                    .requestMatchers("/actuator/**")
                    .hasRole("ADMIN")
                    .anyRequest()
                    .denyAll())
        .oauth2ResourceServer(
            oauth -> oauth.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())))
        .build();
  }

  private JwtAuthenticationConverter jwtAuthenticationConverter() {
    JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
    converter.setJwtGrantedAuthoritiesConverter(new RealmRolesConverter());
    converter.setPrincipalClaimName("preferred_username");
    return converter;
  }

  static class RealmRolesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {
    private final JwtGrantedAuthoritiesConverter scopes = new JwtGrantedAuthoritiesConverter();

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
      Set<GrantedAuthority> authorities = new HashSet<>();
      Collection<GrantedAuthority> scopeAuthorities = scopes.convert(jwt);
      if (scopeAuthorities != null) authorities.addAll(scopeAuthorities);

      Object realmAccess = jwt.getClaim("realm_access");
      if (realmAccess instanceof Map<?, ?> map) {
        Object roles = map.get("roles");
        if (roles instanceof Collection<?> collection) {
          authorities.addAll(
              collection.stream()
                  .map(Object::toString)
                  .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                  .collect(Collectors.toSet()));
        }
      }
      return authorities;
    }
  }
}
