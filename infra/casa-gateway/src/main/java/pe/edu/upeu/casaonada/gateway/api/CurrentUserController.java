package pe.edu.upeu.casaonada.gateway.api;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class CurrentUserController {

    @GetMapping("/me")
    public Map<String, Object> me(@AuthenticationPrincipal Jwt jwt) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("subject", jwt.getSubject());
        result.put("username", jwt.getClaimAsString("preferred_username"));
        result.put("roles", realmRoles(jwt));
        result.put("issuedAt", jwt.getIssuedAt());
        result.put("expiresAt", jwt.getExpiresAt());
        result.put("issuer", jwt.getIssuer());
        return result;
    }

    private List<String> realmRoles(Jwt jwt) {
        Object realmAccess = jwt.getClaim("realm_access");
        if (realmAccess instanceof Map<?, ?> map && map.get("roles") instanceof Collection<?> roles) {
            return roles.stream().map(Object::toString).sorted().toList();
        }
        return List.of();
    }
}
