package pe.edu.upeu.casaonada.agente.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.casaonada.agente.domain.EstadoAgente;
import pe.edu.upeu.casaonada.agente.dto.AgenteRequest;
import pe.edu.upeu.casaonada.agente.dto.AgenteResponse;
import pe.edu.upeu.casaonada.agente.service.AgenteService;
import pe.edu.upeu.casaonada.shared.EmailTemplates;
import pe.edu.upeu.casaonada.shared.KeycloakAdmin;
import pe.edu.upeu.casaonada.shared.Mailer;

/** El administrador da de alta un agente con usuario y clave para ingresar al panel. */
@RestController
@RequestMapping("/api/v1/agentes")
public class AccesoController {
  private final AgenteService service;
  private final KeycloakAdmin keycloak;
  private final Mailer mailer;

  public AccesoController(
      AgenteService service,
      @Value("${app.keycloak.url:http://localhost:28090}") String kcUrl,
      @Value("${app.keycloak.realm:casa-o-nada}") String realm,
      @Value("${app.keycloak.admin-user:admin}") String adminUser,
      @Value("${app.keycloak.admin-password:admin}") String adminPassword,
      @Value("${app.mail.host:localhost}") String mailHost,
      @Value("${app.mail.port:21025}") int mailPort,
      @Value("${app.mail.user:}") String mailUser,
      @Value("${app.mail.password:}") String mailPass,
      @Value("${app.mail.from:CASAS <no-reply@casas.example>}") String mailFrom,
      @Value("${app.mail.enabled:true}") boolean mailOn) {
    this.mailer = new Mailer(mailHost, mailPort, mailUser, mailPass, mailFrom, mailOn);
    this.service = service;
    this.keycloak = new KeycloakAdmin(kcUrl, realm, adminUser, adminPassword);
  }

  public record Acceso(
      @NotBlank @Size(max = 100) String nombres,
      @NotBlank @Size(max = 100) String apellidos,
      @NotBlank @Email @Size(max = 160) String email,
      @Size(max = 30) String telefono,
      @NotBlank @Size(max = 80) String matricula,
      @NotBlank @Size(min = 8, max = 72) String password) {}

  public record Estado(@NotNull EstadoAgente estado) {}

  @PatchMapping("/{id}/estado")
  @PreAuthorize("hasRole('ADMIN')")
  public AgenteResponse estado(@PathVariable Long id, @Valid @RequestBody Estado b) {
    var a = service.buscar(id);
    keycloak.habilitar(a.email(), b.estado() == EstadoAgente.ACTIVO);
    return service.actualizar(
        id,
        new AgenteRequest(
            a.keycloakUserId(),
            a.nombres(),
            a.apellidos(),
            a.email(),
            a.telefono(),
            a.matricula(),
            b.estado()));
  }

  @PostMapping("/acceso")
  @PreAuthorize("hasRole('ADMIN')")
  @ResponseStatus(HttpStatus.CREATED)
  public AgenteResponse crear(@Valid @RequestBody Acceso r) {
    var email = r.email().trim().toLowerCase();
    var kcId =
        keycloak.crearUsuario(
            email, r.nombres().trim(), r.apellidos().trim(), r.password(), "AGENTE", Map.of());
    try {
      var creado = service.crear(
          new AgenteRequest(
              kcId,
              r.nombres().trim(),
              r.apellidos().trim(),
              email,
              r.telefono(),
              r.matricula().trim(),
              EstadoAgente.ACTIVO));
      mailer.send(email, "Tu acceso de agente en CASAS", EmailTemplates.accesoAgente(r.nombres().trim(), email));
      return creado;
    } catch (RuntimeException e) {
      keycloak.eliminarUsuario(kcId);
      throw e;
    }
  }
}
