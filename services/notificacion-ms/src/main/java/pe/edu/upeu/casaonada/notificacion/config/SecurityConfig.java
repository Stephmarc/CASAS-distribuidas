package pe.edu.upeu.casaonada.notificacion.config;

import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.SecurityFilterChain;
import java.util.*;
@Configuration @EnableMethodSecurity
public class SecurityConfig {
 @Bean SecurityFilterChain security(HttpSecurity http)throws Exception{
  var converter=new JwtAuthenticationConverter();
  converter.setJwtGrantedAuthoritiesConverter(jwt->{List<org.springframework.security.core.GrantedAuthority> a=new ArrayList<>();Object realm=jwt.getClaim("realm_access");if(realm instanceof Map<?,?> m && m.get("roles") instanceof Collection<?> roles)roles.forEach(r->a.add(new SimpleGrantedAuthority("ROLE_"+r)));return a;});
  return http.csrf(c->c.disable()).sessionManagement(s->s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
   .authorizeHttpRequests(a->a.dispatcherTypeMatchers(jakarta.servlet.DispatcherType.ERROR).permitAll().requestMatchers("/actuator/health",
                        "/actuator/health/**",
                        "/actuator/prometheus","/actuator/info","/v3/api-docs/**","/swagger-ui/**","/swagger-ui.html").permitAll()
    .requestMatchers("/api/v1/operaciones/**").hasRole("ADMIN")
    .requestMatchers("/actuator/**").hasRole("ADMIN")

    .requestMatchers(HttpMethod.POST,"/api/v1/notificaciones/*/leer").hasAnyRole("CLIENTE","ADMIN")
    .requestMatchers(HttpMethod.GET,"/api/v1/notificaciones/**").hasAnyRole("CLIENTE","AGENTE","ADMIN")
    .requestMatchers(HttpMethod.POST,"/api/v1/notificaciones").hasAnyRole("ADMIN")
    .requestMatchers("/api/v1/notificaciones/**").hasRole("ADMIN").anyRequest().denyAll())
   .oauth2ResourceServer(o->o.jwt(j->j.jwtAuthenticationConverter(converter))).build();
 }
}
