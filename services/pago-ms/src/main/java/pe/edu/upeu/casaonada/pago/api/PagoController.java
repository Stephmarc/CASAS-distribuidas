package pe.edu.upeu.casaonada.pago.api;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.casaonada.pago.domain.EstadoPago;
import pe.edu.upeu.casaonada.pago.dto.*;
import pe.edu.upeu.casaonada.pago.service.PagoService;

@RestController
@RequestMapping("/api/v1/pagos")
public class PagoController {
  private final PagoService s;
  private final pe.edu.upeu.casaonada.pago.service.PagoApplication application;

  public PagoController(PagoService s, pe.edu.upeu.casaonada.pago.service.PagoApplication a) {
    this.s = s;
    application = a;
  }

  @PostMapping
  public ResponseEntity<PagoResponse> crear(
      @RequestHeader("X-Idempotency-Key") String key, @Valid @RequestBody PagoRequest r) {
    var x = application.crear(r, key);
    return ResponseEntity.created(URI.create("/api/v1/pagos/" + x.id())).body(x);
  }

  @GetMapping
  public List<PagoResponse> listar() {
    return s.listar();
  }

  @GetMapping("/{id}/seguimiento")
  public Map<String, Object> seguimiento(@PathVariable Long id) {
    return s.seguimiento(id);
  }

  @GetMapping("/{id}")
  public PagoResponse buscar(@PathVariable Long id) {
    return s.buscar(id);
  }

  public record Simulacion(EstadoPago estado) {}

  @PostMapping("/{id}/simular")
  @PreAuthorize("hasRole('ADMIN')")
  public PagoResponse simular(@PathVariable Long id, @RequestBody Simulacion r) {
    return s.simular(id, r.estado());
  }
}
