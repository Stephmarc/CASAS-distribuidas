package pe.edu.upeu.casaonada.propiedad.service;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.casaonada.propiedad.dto.*;
import pe.edu.upeu.casaonada.propiedad.entity.OutboxEvent;
import pe.edu.upeu.casaonada.propiedad.entity.Propiedad;
import pe.edu.upeu.casaonada.propiedad.enums.EstadoPropiedad;
import pe.edu.upeu.casaonada.propiedad.exception.ConflictException;
import pe.edu.upeu.casaonada.propiedad.exception.PropiedadNotFoundException;
import pe.edu.upeu.casaonada.propiedad.repository.OutboxEventRepository;
import pe.edu.upeu.casaonada.propiedad.repository.PropiedadRepository;
import java.time.Instant;
import java.util.UUID;

@Service
public class PropiedadService {
    private final PropiedadRepository repository;
    private final OutboxEventRepository outboxRepository;
    private final JsonMapper objectMapper;

    public PropiedadService(PropiedadRepository repository,
                            OutboxEventRepository outboxRepository,
                            JsonMapper objectMapper) {
        this.repository = repository;
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public Page<PropiedadResponse> listar(String ciudad, String distrito,
            EstadoPropiedad estado, Pageable pageable) {
        Specification<Propiedad> spec = (root, query, cb) -> cb.conjunction();
        if (ciudad != null && !ciudad.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("ciudad")), "%" + ciudad.trim().toLowerCase() + "%"));
        }
        if (distrito != null && !distrito.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("distrito")), "%" + distrito.trim().toLowerCase() + "%"));
        }
        if (estado != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("estado"), estado));
        } else {
            spec = spec.and((root, query, cb) -> cb.notEqual(root.get("estado"), EstadoPropiedad.DESACTIVADA));
        }
        return repository.findAll(spec, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public PropiedadResponse buscarPorId(Long id) {
        return toResponse(obtener(id));
    }

    @Transactional
    public PropiedadResponse crear(PropiedadCreateRequest request) {
        Propiedad p = new Propiedad();
        p.setTitulo(request.titulo());
        p.setDescripcion(request.descripcion());
        p.setTipoInmueble(request.tipoInmueble());
        p.setTipoOperacion(request.tipoOperacion());
        p.setPrecio(request.precio());
        p.setMoneda(request.moneda());
        p.setAreaM2(request.areaM2());
        p.setDormitorios(request.dormitorios());
        p.setBanos(request.banos());
        p.setDireccion(request.direccion());
        p.setDistrito(request.distrito());
        p.setCiudad(request.ciudad());
        p.setPropietarioId(request.propietarioId());
        p.setEstado(EstadoPropiedad.DISPONIBLE);
        Propiedad guardada = repository.save(p);
        registrarEvento("propiedad.creada", guardada, null, guardada.getEstado());
        return toResponse(guardada);
    }

    @Transactional
    public PropiedadResponse actualizar(Long id, PropiedadUpdateRequest request) {
        Propiedad p = obtener(id);
        if (!p.getVersion().equals(request.version())) {
            throw new ConflictException("Version desactualizada. Versión actual: " + p.getVersion());
        }
        if (p.getEstado() == EstadoPropiedad.DESACTIVADA) {
            throw new ConflictException("No se puede editar una propiedad desactivada");
        }
        p.setTitulo(request.titulo());
        p.setDescripcion(request.descripcion());
        p.setTipoInmueble(request.tipoInmueble());
        p.setTipoOperacion(request.tipoOperacion());
        p.setPrecio(request.precio());
        p.setMoneda(request.moneda());
        p.setAreaM2(request.areaM2());
        p.setDormitorios(request.dormitorios());
        p.setBanos(request.banos());
        p.setDireccion(request.direccion());
        p.setDistrito(request.distrito());
        p.setCiudad(request.ciudad());
        p.setPropietarioId(request.propietarioId());
        Propiedad guardada = repository.save(p);
        registrarEvento("propiedad.actualizada", guardada, null, guardada.getEstado());
        return toResponse(guardada);
    }

    @Transactional
    public PropiedadResponse cambiarEstado(Long id, EstadoPropiedad nuevoEstado) {
        Propiedad p = obtener(id);
        EstadoPropiedad anterior = p.getEstado();
        validarTransicion(anterior, nuevoEstado);
        p.setEstado(nuevoEstado);
        Propiedad guardada = repository.save(p);
        registrarEvento("propiedad.estado.cambiado", guardada, anterior, nuevoEstado);
        return toResponse(guardada);
    }

    @Transactional
    public void desactivar(Long id) {
        Propiedad p = obtener(id);
        if (p.getEstado() == EstadoPropiedad.RESERVADA) {
            throw new ConflictException("No se puede desactivar una propiedad reservada");
        }
        EstadoPropiedad anterior = p.getEstado();
        p.setEstado(EstadoPropiedad.DESACTIVADA);
        Propiedad guardada = repository.save(p);
        registrarEvento("propiedad.desactivada", guardada, anterior, EstadoPropiedad.DESACTIVADA);
    }

    private void validarTransicion(EstadoPropiedad actual, EstadoPropiedad nuevo) {
        if (nuevo == null || actual == nuevo) {
            throw new ConflictException("El nuevo estado debe ser diferente del estado actual");
        }
        boolean valida = switch (actual) {
            case DISPONIBLE -> nuevo == EstadoPropiedad.RESERVADA || nuevo == EstadoPropiedad.VENDIDA
                    || nuevo == EstadoPropiedad.ALQUILADA || nuevo == EstadoPropiedad.DESACTIVADA;
            case RESERVADA -> nuevo == EstadoPropiedad.DISPONIBLE || nuevo == EstadoPropiedad.VENDIDA
                    || nuevo == EstadoPropiedad.ALQUILADA;
            case VENDIDA, ALQUILADA, DESACTIVADA -> false;
        };
        if (!valida) throw new ConflictException("Transición de estado no permitida: " + actual + " -> " + nuevo);
    }

    private Propiedad obtener(Long id) {
        return repository.findById(id).orElseThrow(() -> new PropiedadNotFoundException(id));
    }

    private PropiedadResponse toResponse(Propiedad p) {
        return new PropiedadResponse(p.getId(), p.getTitulo(), p.getDescripcion(),
                p.getTipoInmueble(), p.getTipoOperacion(), p.getPrecio(), p.getMoneda(),
                p.getAreaM2(), p.getDormitorios(), p.getBanos(), p.getDireccion(),
                p.getDistrito(), p.getCiudad(), p.getEstado(), p.getPropietarioId(),
                p.getVersion(), p.getFechaCreacion(), p.getFechaActualizacion());
    }

    private void registrarEvento(String tipo, Propiedad p,
                                 EstadoPropiedad anterior, EstadoPropiedad nuevo) {
        try {
            Instant now = Instant.now();
            PropiedadEvento event = new PropiedadEvento(UUID.randomUUID(), tipo,
                    p.getId(), anterior, nuevo, now);
            String payload = objectMapper.writeValueAsString(event);
            outboxRepository.save(new OutboxEvent(event.eventId(), "Propiedad",
                    p.getId().toString(), tipo, payload, now));
        } catch (JacksonException ex) {
            throw new IllegalStateException("No se pudo serializar el evento de propiedad", ex);
        }
    }
}
