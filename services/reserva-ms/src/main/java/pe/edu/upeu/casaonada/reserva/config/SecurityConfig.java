package pe.edu.upeu.casaonada.reserva.config;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty; import org.springframework.context.annotation.Bean; import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter; import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity; import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.GrantedAuthority; import org.springframework.security.core.authority.SimpleGrantedAuthority; import org.springframework.security.oauth2.jwt.Jwt; import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter; import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter; import org.springframework.security.web.SecurityFilterChain;
import java.util.*; import java.util.stream.Collectors;
@Configuration
public class SecurityConfig {
 @Bean @ConditionalOnProperty(name="app.security.enabled",havingValue="false",matchIfMissing=true)
 SecurityFilterChain open(HttpSecurity http) throws Exception { return http.csrf(c->c.disable()).authorizeHttpRequests(a->a.anyRequest().permitAll()).build(); }
 @Configuration @EnableMethodSecurity @ConditionalOnProperty(name="app.security.enabled",havingValue="true")
 static class JwtSecurity {
  @Bean SecurityFilterChain secure(HttpSecurity http) throws Exception { return http.csrf(c->c.disable()).authorizeHttpRequests(a->a.requestMatchers("/actuator/health","/v3/api-docs/**","/swagger-ui/**","/swagger-ui.html").permitAll().anyRequest().authenticated()).oauth2ResourceServer(o->o.jwt(j->j.jwtAuthenticationConverter(jwtConverter()))).build(); }
  private JwtAuthenticationConverter jwtConverter() { JwtAuthenticationConverter c=new JwtAuthenticationConverter(); c.setJwtGrantedAuthoritiesConverter(new RealmRolesConverter()); return c; }
 }
 static class RealmRolesConverter implements Converter<Jwt,Collection<GrantedAuthority>> {
  private final JwtGrantedAuthoritiesConverter scopes=new JwtGrantedAuthoritiesConverter();
  public Collection<GrantedAuthority> convert(Jwt jwt) {
   Set<GrantedAuthority> out=new HashSet<>(scopes.convert(jwt)); Object ra=jwt.getClaim("realm_access");
   if(ra instanceof Map<?,?> map) { Object roles=map.get("roles"); if(roles instanceof Collection<?> rs) out.addAll(rs.stream().map(Object::toString).map(r->new SimpleGrantedAuthority("ROLE_"+r)).collect(Collectors.toSet())); }
   return out;
  }
 }
}
