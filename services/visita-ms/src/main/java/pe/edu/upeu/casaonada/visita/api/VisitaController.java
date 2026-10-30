package pe.edu.upeu.casaonada.visita.api;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.casaonada.shared.Identidad;
import pe.edu.upeu.casaonada.visita.domain.EstadoVisita;
import pe.edu.upeu.casaonada.visita.dto.*;
import pe.edu.upeu.casaonada.visita.service.VisitaService;

@RestController
@RequestMapping("/api/v1/visitas")
public class VisitaController {
  private final VisitaService s;

  public VisitaController(VisitaService s) {
    this.s = s;
  }

  @PostMapping
  public ResponseEntity<VisitaResponse> crear(@Valid @RequestBody VisitaRequest r) {
    var normalized =
        new VisitaRequest(
            Identidad.efectivo(r.clienteId()),
            r.propiedadId(),
            r.agenteId(),
            r.fechaHora(),
            r.observaciones(),
            r.estado());
    var x = s.crear(normalized);
    return ResponseEntity.created(URI.create("/api/v1/visitas/" + x.id())).body(x);
  }

  @GetMapping
  public List<VisitaResponse> listar() {
    return s.listar().stream()
        .filter(
            v ->
                Identidad.rol("ADMIN")
                    || Identidad.rol("AGENTE")
                    || v.clienteId().equals(Identidad.cliente()))
        .toList();
  }

  @GetMapping("/{id}")
  public VisitaResponse buscar(@PathVariable Long id) {
    var v = s.buscar(id);
    Identidad.propietario(v.clienteId());
    return v;
  }

  @PostMapping("/{id}/cancelar")
  public VisitaResponse cancelar(@PathVariable Long id) {
    return s.cancelar(id);
  }

  @PutMapping("/{id}")
  public VisitaResponse actualizar(@PathVariable Long id, @Valid @RequestBody VisitaRequest r) {
    return s.actualizar(id, r);
  }

  @PatchMapping("/{id}/estado")
  public VisitaResponse estado(@PathVariable Long id, @RequestBody Map<String, String> b) {
    return s.cambiarEstado(
        id, EstadoVisita.valueOf(pe.edu.upeu.casaonada.shared.DomainRules.requiredState(b)));
  }
}
