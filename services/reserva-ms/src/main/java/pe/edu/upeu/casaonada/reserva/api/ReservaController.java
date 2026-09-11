package pe.edu.upeu.casaonada.reserva.api;
import pe.edu.upeu.casaonada.reserva.domain.EstadoReserva; import pe.edu.upeu.casaonada.reserva.dto.*; import pe.edu.upeu.casaonada.reserva.service.ReservaService; import jakarta.validation.Valid; import org.springframework.http.*; import org.springframework.web.bind.annotation.*; import java.net.URI; import java.util.*;
@RestController @RequestMapping("/api/v1/reservas") public class ReservaController {
 private final ReservaService s; public ReservaController(ReservaService s){this.s=s;}
 @PostMapping public ResponseEntity<ReservaResponse> crear(@Valid @RequestBody ReservaRequest r){var x=s.crear(r);return ResponseEntity.created(URI.create("/api/v1/reservas/"+x.id())).body(x);}
 @GetMapping public List<ReservaResponse> listar(){return s.listar();} @GetMapping("/{id}") public ReservaResponse buscar(@PathVariable Long id){return s.buscar(id);}
 @PatchMapping("/{id}/estado") public ReservaResponse estado(@PathVariable Long id,@RequestBody Map<String,String> body){return s.actualizarEstado(id,EstadoReserva.valueOf(body.get("estado")));}
 @PostMapping("/{id}/cancelar") public ReservaResponse cancelar(@PathVariable Long id){return s.actualizarEstado(id,EstadoReserva.CANCELADA);}
 @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void eliminar(@PathVariable Long id){s.eliminar(id);}
}
