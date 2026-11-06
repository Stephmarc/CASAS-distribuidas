package pe.edu.upeu.casaonada.shared;

import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

/** Crea usuarios de laboratorio en Keycloak (registro de clientes y alta de agentes). */
public final class KeycloakAdmin {
  private final RestClient http;
  private final String realm;
  private final String adminUser;
  private final String adminPassword;

  public KeycloakAdmin(String baseUrl, String realm, String adminUser, String adminPassword) {
    this.http = RestClient.create(baseUrl);
    this.realm = realm;
    this.adminUser = adminUser;
    this.adminPassword = adminPassword;
  }

  @SuppressWarnings("unchecked")
  private String token() {
    var form = new LinkedMultiValueMap<String, String>();
    form.add("client_id", "admin-cli");
    form.add("grant_type", "password");
    form.add("username", adminUser);
    form.add("password", adminPassword);
    Map<String, Object> r =
        http.post()
            .uri("/realms/master/protocol/openid-connect/token")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(form)
            .retrieve()
            .body(Map.class);
    return String.valueOf(Objects.requireNonNull(r).get("access_token"));
  }

  /** Keycloak 26 ignora atributos no declarados (cliente_id) salvo que se habiliten. */
  @SuppressWarnings("unchecked")
  private void permitirAtributos(String t) {
    Map<String, Object> profile =
        http.get()
            .uri("/admin/realms/{r}/users/profile", realm)
            .headers(h -> h.setBearerAuth(t))
            .retrieve()
            .body(Map.class);
    if (profile != null && !"ENABLED".equals(profile.get("unmanagedAttributePolicy"))) {
      profile.put("unmanagedAttributePolicy", "ENABLED");
      http.put()
          .uri("/admin/realms/{r}/users/profile", realm)
          .headers(h -> h.setBearerAuth(t))
          .contentType(MediaType.APPLICATION_JSON)
          .body(profile)
          .retrieve()
          .toBodilessEntity();
    }
  }

  /** Devuelve el id del usuario creado en Keycloak. */
  @SuppressWarnings("unchecked")
  public String crearUsuario(
      String email,
      String nombres,
      String apellidos,
      String password,
      String rol,
      Map<String, List<String>> atributos) {
    try {
      var t = token();
      if (!atributos.isEmpty()) permitirAtributos(t);
      var user = new LinkedHashMap<String, Object>();
      user.put("username", email);
      user.put("email", email);
      user.put("firstName", nombres);
      user.put("lastName", apellidos == null || apellidos.isBlank() ? "-" : apellidos);
      user.put("enabled", true);
      user.put("emailVerified", true);
      if (!atributos.isEmpty()) user.put("attributes", atributos);
      user.put(
          "credentials", List.of(Map.of("type", "password", "value", password, "temporary", false)));
      var created =
          http.post()
              .uri("/admin/realms/{r}/users", realm)
              .headers(h -> h.setBearerAuth(t))
              .contentType(MediaType.APPLICATION_JSON)
              .body(user)
              .retrieve()
              .toBodilessEntity();
      var location = Objects.requireNonNull(created.getHeaders().getLocation()).getPath();
      var id = location.substring(location.lastIndexOf('/') + 1);
      Map<String, Object> role =
          http.get()
              .uri("/admin/realms/{r}/roles/{n}", realm, rol)
              .headers(h -> h.setBearerAuth(t))
              .retrieve()
              .body(Map.class);
      http.post()
          .uri("/admin/realms/{r}/users/{u}/role-mappings/realm", realm, id)
          .headers(h -> h.setBearerAuth(t))
          .contentType(MediaType.APPLICATION_JSON)
          .body(List.of(role))
          .retrieve()
          .toBodilessEntity();
      return id;
    } catch (HttpClientErrorException.Conflict e) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un usuario con ese correo");
    } catch (HttpClientErrorException.BadRequest e) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Keycloak rechazo los datos (revisa la clave y el correo)");
    } catch (RuntimeException e) {
      if (e instanceof ResponseStatusException) throw e;
      throw new ResponseStatusException(
          HttpStatus.SERVICE_UNAVAILABLE, "No se pudo crear el usuario en Keycloak");
    }
  }

  /** Habilita o deshabilita el acceso de un usuario (busqueda exacta por correo). */
  @SuppressWarnings("unchecked")
  public void habilitar(String email, boolean enabled) {
    try {
      var t = token();
      List<Map<String, Object>> found =
          http.get()
              .uri(
                  uriBuilder ->
                      uriBuilder
                          .path("/admin/realms/{r}/users")
                          .queryParam("email", email)
                          .queryParam("exact", true)
                          .build(realm))
              .headers(h -> h.setBearerAuth(t))
              .retrieve()
              .body(List.class);
      if (found == null || found.isEmpty()) return;
      var id = String.valueOf(found.get(0).get("id"));
      http.put()
          .uri("/admin/realms/{r}/users/{u}", realm, id)
          .headers(h -> h.setBearerAuth(t))
          .contentType(MediaType.APPLICATION_JSON)
          .body(Map.of("enabled", enabled))
          .retrieve()
          .toBodilessEntity();
    } catch (RuntimeException e) {
      throw new ResponseStatusException(
          HttpStatus.SERVICE_UNAVAILABLE, "No se pudo actualizar el acceso en Keycloak");
    }
  }

  public void eliminarUsuario(String id) {
    try {
      http.delete()
          .uri("/admin/realms/{r}/users/{u}", realm, id)
          .headers(h -> h.setBearerAuth(token()))
          .retrieve()
          .toBodilessEntity();
    } catch (RuntimeException ignored) {
      // compensacion de mejor esfuerzo
    }
  }
}
