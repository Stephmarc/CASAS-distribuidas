package pe.edu.upeu.casaonada.propiedad.api;
import pe.edu.upeu.casaonada.propiedad.domain.*; import pe.edu.upeu.casaonada.propiedad.dto.*; import pe.edu.upeu.casaonada.propiedad.service.PropiedadService; import jakarta.validation.Valid; import org.springframework.http.*; import org.springframework.web.bind.annotation.*; import java.math.BigDecimal; import java.net.URI; import java.util.*;
@RestController @RequestMapping("/api/v1/propiedades") public class PropiedadController {
 private final PropiedadService s; public PropiedadController(PropiedadService s){this.s=s;}
 @PostMapping public ResponseEntity<PropiedadResponse> crear(@Valid @RequestBody PropiedadRequest r){var x=s.crear(r);return ResponseEntity.created(URI.create("/api/v1/propiedades/"+x.id())).body(x);}
 @GetMapping public List<PropiedadResponse> listar(@RequestParam(required=false) String ciudad,@RequestParam(required=false) TipoOperacion tipoOperacion,@RequestParam(required=false) EstadoPropiedad estado,@RequestParam(required=false) BigDecimal precioMin,@RequestParam(required=false) BigDecimal precioMax){return s.listar(ciudad,tipoOperacion,estado,precioMin,precioMax);}
 @GetMapping("/{id}") public PropiedadResponse buscar(@PathVariable Long id){return s.buscar(id);}
 @PutMapping("/{id}") public PropiedadResponse actualizar(@PathVariable Long id,@Valid @RequestBody PropiedadRequest r){return s.actualizar(id,r);}
 @PatchMapping("/{id}/estado") public PropiedadResponse estado(@PathVariable Long id,@RequestBody Map<String,String> b){return s.cambiarEstado(id,EstadoPropiedad.valueOf(b.get("estado")));}
 @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void eliminar(@PathVariable Long id){s.eliminar(id);}
}
