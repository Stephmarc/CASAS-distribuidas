package pe.edu.upeu.casaonada.cliente.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;
import pe.edu.upeu.casaonada.cliente.domain.Cliente;
import pe.edu.upeu.casaonada.cliente.repository.ClienteRepository;
import pe.edu.upeu.casaonada.shared.EmailTemplates;
import pe.edu.upeu.casaonada.shared.KeycloakAdmin;
import pe.edu.upeu.casaonada.shared.Mailer;

/** Registro publico de clientes (con DNI) y consulta de datos por DNI. */
@RestController
@RequestMapping("/api/v1/clientes")
public class RegistroController {
  private final ClienteRepository repo;
  private final KeycloakAdmin keycloak;
  private final Mailer mailer;
  private final String dniUrl;
  private final String dniToken;

  public RegistroController(
      ClienteRepository repo,
      @Value("${app.keycloak.url:http://localhost:28090}") String kcUrl,
      @Value("${app.keycloak.realm:casa-o-nada}") String realm,
      @Value("${app.keycloak.admin-user:admin}") String adminUser,
      @Value("${app.keycloak.admin-password:admin}") String adminPassword,
      @Value("${app.dni.url:https://api.apis.net.pe/v2/reniec/dni}") String dniUrl,
      @Value("${app.dni.token:}") String dniToken,
      @Value("${app.mail.host:localhost}") String mailHost,
      @Value("${app.mail.port:21025}") int mailPort,
      @Value("${app.mail.user:}") String mailUser,
      @Value("${app.mail.password:}") String mailPass,
      @Value("${app.mail.from:CASAS <no-reply@casas.example>}") String mailFrom,
      @Value("${app.mail.enabled:true}") boolean mailOn) {
    this.mailer = new Mailer(mailHost, mailPort, mailUser, mailPass, mailFrom, mailOn);
    this.repo = repo;
    this.keycloak = new KeycloakAdmin(kcUrl, realm, adminUser, adminPassword);
    this.dniUrl = dniUrl;
    this.dniToken = dniToken;
  }

  public record Registro(
      @NotBlank @Pattern(regexp = "[0-9]{8}", message = "El DNI tiene 8 digitos") String dni,
      @NotBlank @Size(max = 100) String nombres,
      @NotBlank @Size(max = 100) String apellidos,
      @NotBlank @Email @Size(max = 200) String email,
      @Size(max = 30) String telefono,
      @NotBlank @Size(min = 8, max = 72) String password) {}

  @PostMapping("/registro")
  @Transactional
  @ResponseStatus(HttpStatus.CREATED)
  public Map<String, Object> registrar(@Valid @RequestBody Registro r) {
    var email = r.email().trim().toLowerCase();
    if (repo.existsByDni(r.dni()))
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Ese DNI ya esta registrado");
    if (repo.existsByEmailIgnoreCase(email))
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Ese correo ya esta registrado");
    long id = repo.maxId() + 1;
    var kcId =
        keycloak.crearUsuario(
            email,
            r.nombres().trim(),
            r.apellidos().trim(),
            r.password(),
            "CLIENTE",
            Map.of("cliente_id", List.of(String.valueOf(id))));
    try {
      var c = new Cliente();
      c.id = id;
      c.dni = r.dni();
      c.nombres = (r.nombres().trim() + " " + r.apellidos().trim()).trim();
      c.email = email;
      c.telefono = r.telefono();
      c.activo = true;
      repo.saveAndFlush(c);
    } catch (RuntimeException e) {
      keycloak.eliminarUsuario(kcId);
      throw e;
    }
    mailer.send(email, "Bienvenido a CASAS", EmailTemplates.bienvenida(r.nombres().trim()));
    return Map.of("id", id, "email", email);
  }

  public record Activo(boolean activo) {}

  @PatchMapping("/{id}/activo")
  @Transactional
  @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
  public Cliente activo(@PathVariable Long id, @RequestBody Activo b) {
    var c =
        repo.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado"));
    keycloak.habilitar(c.email, b.activo());
    c.activo = b.activo();
    return repo.save(c);
  }

  /** Consulta RENIEC mediante un proveedor externo; requiere token (app.dni.token). */
  @GetMapping("/dni/{dni}")
  @SuppressWarnings("unchecked")
  public Map<String, Object> dni(@PathVariable String dni) {
    if (!dni.matches("[0-9]{8}"))
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El DNI tiene 8 digitos");
    if (dniToken.isBlank())
      throw new ResponseStatusException(
          HttpStatus.SERVICE_UNAVAILABLE, "Consulta de DNI no configurada; ingresa tus datos");
    try {
      Map<String, Object> r =
          RestClient.create()
              .get()
              .uri(dniUrl + "?numero={n}", dni)
              .headers(h -> h.setBearerAuth(dniToken))
              .retrieve()
              .body(Map.class);
      if (r == null) throw new IllegalStateException();
      var nombres = text(r, "nombres", "first_name");
      var paterno = text(r, "apellidoPaterno", "first_last_name");
      var materno = text(r, "apellidoMaterno", "second_last_name");
      if (nombres.isBlank())
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "DNI no encontrado");
      return Map.of("dni", dni, "nombres", nombres, "apellidos", (paterno + " " + materno).trim());
    } catch (ResponseStatusException e) {
      throw e;
    } catch (RuntimeException e) {
      throw new ResponseStatusException(
          HttpStatus.SERVICE_UNAVAILABLE, "No se pudo consultar el DNI; ingresa tus datos");
    }
  }

  private static String text(Map<String, Object> m, String... keys) {
    for (var k : keys) if (m.get(k) != null) return String.valueOf(m.get(k)).trim();
    return "";
  }
}
