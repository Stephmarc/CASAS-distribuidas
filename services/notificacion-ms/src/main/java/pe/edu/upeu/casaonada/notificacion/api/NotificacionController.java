package pe.edu.upeu.casaonada.notificacion.api;

import pe.edu.upeu.casaonada.notificacion.domain.Notificacion;
import pe.edu.upeu.casaonada.notificacion.repository.NotificacionRepository;
import pe.edu.upeu.casaonada.shared.Identidad;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
@RestController @RequestMapping("/api/v1/notificaciones") public class NotificacionController {
 private final NotificacionRepository repo;public NotificacionController(NotificacionRepository r){repo=r;}
 @GetMapping public List<Notificacion> listar(){return Identidad.rol("ADMIN")||Identidad.rol("AGENTE")?repo.findAll():repo.findByClienteIdOrderByCreatedAtDesc(Identidad.cliente());}
 @PostMapping("/{id}/leer") @Transactional public Notificacion leer(@PathVariable Long id){var n=repo.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));Identidad.propietario(n.clienteId);n.leida=true;return repo.save(n);}
}
