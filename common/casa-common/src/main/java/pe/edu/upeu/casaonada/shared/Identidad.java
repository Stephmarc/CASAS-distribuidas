package pe.edu.upeu.casaonada.shared;

import java.util.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

public final class Identidad {
  private Identidad() {}

  public static Jwt jwt() {
    var a = SecurityContextHolder.getContext().getAuthentication();
    return a != null && a.getPrincipal() instanceof Jwt j ? j : null;
  }

  public static boolean rol(String role) {
    var a = SecurityContextHolder.getContext().getAuthentication();
    return a != null
        && a.getAuthorities().stream().anyMatch(x -> x.getAuthority().equals("ROLE_" + role));
  }

  public static Long cliente() {
    var j = jwt();
    try {
      Long id = Long.valueOf(Objects.requireNonNull(j).getClaim("cliente_id").toString());
      if (id <= 0) throw new IllegalArgumentException();
      return id;
    } catch (Exception e) {
      throw new AccessDeniedException("El JWT requiere un cliente_id valido");
    }
  }

  public static Long efectivo(Long requested) {
    if (!rol("ADMIN") && !rol("AGENTE")) return cliente();
    if (requested == null) throw new IllegalArgumentException("clienteId es obligatorio");
    return requested;
  }

  public static void propietario(Long id) {
    if (!rol("ADMIN") && !rol("AGENTE") && !cliente().equals(id))
      throw new AccessDeniedException("Operacion de otro cliente");
  }
}
