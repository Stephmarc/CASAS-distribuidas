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

  public record ComprobanteRequest(
      @jakarta.validation.constraints.NotBlank String metodo,
      @jakarta.validation.constraints.NotBlank String nroOperacion,
      @jakarta.validation.constraints.NotBlank String tipo,
      @jakarta.validation.constraints.NotBlank String datos) {}

  public record Revision(String accion, String motivo) {}

  @PostMapping("/{id}/comprobante")
  public PagoResponse comprobante(@PathVariable Long id, @Valid @RequestBody ComprobanteRequest r) {
    byte[] bytes;
    try {
      bytes = Base64.getDecoder().decode(r.datos());
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Captura invalida");
    }
    return s.comprobante(id, r.metodo(), r.nroOperacion(), r.tipo(), bytes);
  }

  @GetMapping("/{id}/comprobante")
  public ResponseEntity<byte[]> verComprobante(@PathVariable Long id) {
    var c = s.verComprobante(id);
    return ResponseEntity.ok().contentType(MediaType.parseMediaType(c.tipo())).body(c.datos());
  }

  @PostMapping("/{id}/revisar")
  @PreAuthorize("hasAnyRole('AGENTE','ADMIN')")
  public PagoResponse revisar(@PathVariable Long id, @RequestBody Revision r) {
    return s.revisar(id, r.accion(), r.motivo());
  }

  @PostMapping("/{id}/simular")
  @PreAuthorize("hasRole('ADMIN')")
  public PagoResponse simular(@PathVariable Long id, @RequestBody Simulacion r) {
    return s.simular(id, r.estado());
  }
}
