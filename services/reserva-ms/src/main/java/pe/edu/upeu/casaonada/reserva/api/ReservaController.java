package pe.edu.upeu.casaonada.reserva.api;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import jakarta.validation.Valid;
import pe.edu.upeu.casaonada.reserva.domain.EstadoReserva;
import pe.edu.upeu.casaonada.reserva.dto.*;
import pe.edu.upeu.casaonada.reserva.service.ReservaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/reservas")
public class ReservaController {
    private final ReservaService service;
    private final CircuitBreakerRegistry circuitBreakerRegistry;

    public ReservaController(ReservaService service, CircuitBreakerRegistry circuitBreakerRegistry) {
        this.service = service;
        this.circuitBreakerRegistry = circuitBreakerRegistry;
    }

    @PostMapping
    public ResponseEntity<ReservaResponse> crear(
            @Valid @RequestBody ReservaRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long clienteId = effectiveClienteId(jwt, request.clienteId());
        var normalizada = new ReservaRequest(
                clienteId,
                request.propiedadId(),
                request.fechaExpiracion(),
                request.montoReserva(),
                request.estado()
        );
        var creada = service.crear(normalizada);
        return ResponseEntity.created(URI.create("/api/v1/reservas/" + creada.id())).body(creada);
    }

    @GetMapping
    public List<ReservaResponse> listar(@AuthenticationPrincipal Jwt jwt) {
        if (hasRole(jwt, "CLIENTE")) {
            return service.listarPorCliente(requiredClienteId(jwt));
        }
        return service.listar();
    }

    @GetMapping("/{id}")
    public ReservaResponse buscar(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        ReservaResponse reserva = service.buscar(id);
        assertOwnerIfCliente(jwt, reserva);
        return reserva;
    }

    @GetMapping("/detalle/{id}")
    public ReservaDetalleResponse detalle(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        ReservaResponse reserva = service.buscar(id);
        assertOwnerIfCliente(jwt, reserva);
        return service.detalle(id);
    }

    @PatchMapping("/{id}/estado")
    public ReservaResponse estado(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return service.actualizarEstado(id, EstadoReserva.valueOf(body.get("estado")));
    }

    @PostMapping("/{id}/cancelar")
    public ReservaResponse cancelar(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        ReservaResponse reserva = service.buscar(id);
        assertOwnerIfCliente(jwt, reserva);
        return service.actualizarEstado(id, EstadoReserva.CANCELADA);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        service.eliminar(id);
    }

    /** Endpoint de apoyo para demostrar CLOSED / OPEN / HALF_OPEN en la práctica. */
    @GetMapping("/_circuit-breaker")
    public Map<String, Object> circuitBreaker() {
        var cb = circuitBreakerRegistry.circuitBreaker("propiedad");
        var metrics = cb.getMetrics();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("name", cb.getName());
        result.put("state", cb.getState().name());
        result.put("failureRate", metrics.getFailureRate());
        result.put("bufferedCalls", metrics.getNumberOfBufferedCalls());
        result.put("failedCalls", metrics.getNumberOfFailedCalls());
        result.put("successfulCalls", metrics.getNumberOfSuccessfulCalls());
        result.put("notPermittedCalls", metrics.getNumberOfNotPermittedCalls());
        return result;
    }

    private Long effectiveClienteId(Jwt jwt, Long requestedClienteId) {
        if (hasRole(jwt, "CLIENTE")) {
            return requiredClienteId(jwt);
        }
        if (requestedClienteId == null) {
            throw new IllegalArgumentException("clienteId es obligatorio para una operación administrativa");
        }
        return requestedClienteId;
    }

    private Long requiredClienteId(Jwt jwt) {
        Object claim = jwt == null ? null : jwt.getClaim("cliente_id");
        if (claim == null) {
            throw new AccessDeniedException("El JWT no contiene el claim cliente_id");
        }
        try {
            return Long.valueOf(claim.toString());
        } catch (NumberFormatException ex) {
            throw new AccessDeniedException("El claim cliente_id del JWT no es válido");
        }
    }

    private void assertOwnerIfCliente(Jwt jwt, ReservaResponse reserva) {
        if (hasRole(jwt, "CLIENTE") && !requiredClienteId(jwt).equals(reserva.clienteId())) {
            throw new AccessDeniedException("La reserva pertenece a otro cliente");
        }
    }

    private boolean hasRole(Jwt jwt, String role) {
        if (jwt == null) return false;
        Object realmAccess = jwt.getClaim("realm_access");
        if (realmAccess instanceof Map<?, ?> map && map.get("roles") instanceof Collection<?> roles) {
            return roles.stream().map(Object::toString).anyMatch(role::equals);
        }
        return false;
    }
}
