package pe.edu.upeu.casaonada.orden.api;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import jakarta.validation.Valid;
import java.util.*;
import pe.edu.upeu.casaonada.orden.dto.OrdenRequest;
import pe.edu.upeu.casaonada.orden.domain.Orden;
import pe.edu.upeu.casaonada.orden.service.OrdenService;
@RestController @RequestMapping("/api/v1/ordenes")
public class OrdenController {
 private final OrdenService service;public OrdenController(OrdenService s){service=s;}
 @PostMapping @ResponseStatus(HttpStatus.ACCEPTED) public Orden crear(@RequestHeader("X-Idempotency-Key") String key,@Valid @RequestBody OrdenRequest r){return service.crear(r,key);}
 @GetMapping public List<Orden> listar(){return service.listar();}
 @GetMapping("/{id}") public Orden buscar(@PathVariable Long id){return service.buscar(id);}
 @PostMapping("/{id}/cancelar") public Orden cancelar(@PathVariable Long id){return service.cancelar(id);}
}
