package pe.edu.upeu.casaonada.propiedad.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import java.util.*;

public class KeycloakRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {
    private final JwtGrantedAuthoritiesConverter scopes = new JwtGrantedAuthoritiesConverter();

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        Set<GrantedAuthority> authorities = new HashSet<>();
        Collection<GrantedAuthority> scopeAuthorities = scopes.convert(jwt);
        if (scopeAuthorities != null) authorities.addAll(scopeAuthorities);

        Object realmClaim = jwt.getClaims().get("realm_access");
        if (realmClaim instanceof Map<?, ?> realm) addRoles(realm.get("roles"), authorities);

        Object resourceClaim = jwt.getClaims().get("resource_access");
        if (resourceClaim instanceof Map<?, ?> resources) {
            resources.values().forEach(value -> {
                if (value instanceof Map<?, ?> client) addRoles(client.get("roles"), authorities);
            });
        }
        return authorities;
    }

    private void addRoles(Object roles, Set<GrantedAuthority> authorities) {
        if (roles instanceof Collection<?> values) {
            values.stream().filter(String.class::isInstance).map(String.class::cast)
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .forEach(authorities::add);
        }
    }
}
