package pe.edu.upeu.casaonada.propiedad.dto;

import pe.edu.upeu.casaonada.propiedad.enums.EstadoPropiedad;
import java.time.Instant;
import java.util.UUID;

public record PropiedadEvento(
    UUID eventId,
    String eventType,
    Long propiedadId,
    EstadoPropiedad estadoAnterior,
    EstadoPropiedad estadoNuevo,
    Instant occurredAt
) { }
