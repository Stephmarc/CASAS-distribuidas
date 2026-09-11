package pe.edu.upeu.casaonada.agente.api;
import pe.edu.upeu.casaonada.agente.dto.*; import pe.edu.upeu.casaonada.agente.service.AgenteService; import jakarta.validation.Valid; import org.springframework.http.*; import org.springframework.web.bind.annotation.*; import java.net.URI; import java.util.List;
@RestController @RequestMapping("/api/v1/agentes") public class AgenteController {
 private final AgenteService service; public AgenteController(AgenteService service){this.service=service;}
 @PostMapping public ResponseEntity<AgenteResponse> crear(@Valid @RequestBody AgenteRequest r){ var x=service.crear(r); return ResponseEntity.created(URI.create("/api/v1/agentes/"+x.id())).body(x); }
 @GetMapping public List<AgenteResponse> listar(){return service.listar();} @GetMapping("/{id}") public AgenteResponse buscar(@PathVariable Long id){return service.buscar(id);}
 @PutMapping("/{id}") public AgenteResponse actualizar(@PathVariable Long id,@Valid @RequestBody AgenteRequest r){return service.actualizar(id,r);}
 @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void eliminar(@PathVariable Long id){service.eliminar(id);}
}
