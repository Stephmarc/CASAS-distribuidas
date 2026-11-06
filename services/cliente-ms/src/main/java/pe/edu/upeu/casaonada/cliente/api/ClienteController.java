package pe.edu.upeu.casaonada.cliente.api;

import pe.edu.upeu.casaonada.cliente.domain.Cliente;
import pe.edu.upeu.casaonada.cliente.repository.ClienteRepository;
import pe.edu.upeu.casaonada.shared.Identidad;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
@RestController @RequestMapping("/api/v1/clientes") public class ClienteController {
 private final ClienteRepository repo;public ClienteController(ClienteRepository r){repo=r;}
 public record Request(@NotNull @Positive Long id,@NotBlank @Size(max=160) String nombres,@NotBlank @Email @Size(max=200) String email,boolean activo){}
 @PostMapping @Transactional @ResponseStatus(HttpStatus.CREATED) public Cliente crear(@Valid @RequestBody Request r){if(repo.existsById(r.id()))throw new ResponseStatusException(HttpStatus.CONFLICT,"Cliente existente");return save(r);}
 @PutMapping("/{id}") @Transactional public Cliente actualizar(@PathVariable Long id,@Valid @RequestBody Request r){if(!id.equals(r.id()))throw new IllegalArgumentException("Identificadores distintos");buscar(id);return save(r);}
 private Cliente save(Request r){var c=repo.findById(r.id()).orElseGet(Cliente::new);c.id=r.id();c.nombres=r.nombres();c.email=r.email();c.activo=r.activo();return repo.save(c);}
 @GetMapping("/{id}") public Cliente buscar(@PathVariable Long id){Identidad.propietario(id);return repo.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Cliente no encontrado"));}
 @GetMapping public List<Cliente> listar(){return Identidad.rol("ADMIN")||Identidad.rol("AGENTE")?repo.findAll():List.of(buscar(Identidad.cliente()));}
}
