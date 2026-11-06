package pe.edu.upeu.casaonada.propiedad.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.casaonada.propiedad.dto.*;
import pe.edu.upeu.casaonada.propiedad.enums.EstadoPropiedad;
import pe.edu.upeu.casaonada.propiedad.service.PropiedadService;

@RestController
@RequestMapping("/api/v1/propiedades")
@Tag(name = "Propiedades", description = "Catálogo inmobiliario CASA O NADA")
public class PropiedadController {
    private final PropiedadService service;
    public PropiedadController(PropiedadService service) { this.service = service; }

    @GetMapping
    @Operation(summary = "Listar propiedades con filtros y paginación")
    public Page<PropiedadResponse> listar(
            @RequestParam(required = false) String ciudad,
            @RequestParam(required = false) String distrito,
            @RequestParam(required = false) EstadoPropiedad estado,
            @PageableDefault(size = 10, sort = "fechaCreacion") Pageable pageable) {
        return service.listar(ciudad, distrito, estado, pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar propiedad por identificador")
    public PropiedadResponse buscar(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','AGENTE')")
    @Operation(summary = "Crear propiedad")
    public PropiedadResponse crear(@Valid @RequestBody PropiedadCreateRequest request) {
        return service.crear(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','AGENTE')")
    @Operation(summary = "Actualizar datos de una propiedad; requiere version")
    public PropiedadResponse actualizar(@PathVariable Long id,
            @Valid @RequestBody PropiedadUpdateRequest request) {
        return service.actualizar(id, request);
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('ADMIN','AGENTE')")
    @Operation(summary = "Cambiar estado de negocio de una propiedad")
    public PropiedadResponse cambiarEstado(@PathVariable Long id,
            @RequestParam EstadoPropiedad estado) {
        return service.cambiarEstado(id, estado);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Desactivar lógicamente una propiedad")
    public void desactivar(@PathVariable Long id) { service.desactivar(id); }
}
