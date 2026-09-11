package pe.edu.upeu.casaonada.cliente.api;
import pe.edu.upeu.casaonada.cliente.dto.*; import pe.edu.upeu.casaonada.cliente.service.ClienteService; import jakarta.validation.Valid; import org.springframework.http.*; import org.springframework.web.bind.annotation.*; import java.net.URI; import java.util.List;
@RestController @RequestMapping("/api/v1/clientes") public class ClienteController {
 private final ClienteService service; public ClienteController(ClienteService service){this.service=service;}
 @PostMapping public ResponseEntity<ClienteResponse> crear(@Valid @RequestBody ClienteRequest r){ var x=service.crear(r); return ResponseEntity.created(URI.create("/api/v1/clientes/"+x.id())).body(x); }
 @GetMapping public List<ClienteResponse> listar(){return service.listar();} @GetMapping("/{id}") public ClienteResponse buscar(@PathVariable Long id){return service.buscar(id);}
 @PutMapping("/{id}") public ClienteResponse actualizar(@PathVariable Long id,@Valid @RequestBody ClienteRequest r){return service.actualizar(id,r);}
 @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void eliminar(@PathVariable Long id){service.eliminar(id);}
}
