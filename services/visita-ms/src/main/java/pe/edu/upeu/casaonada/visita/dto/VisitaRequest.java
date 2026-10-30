package pe.edu.upeu.casaonada.visita.dto;

import jakarta.validation.constraints.*;
import java.time.OffsetDateTime;
import pe.edu.upeu.casaonada.visita.domain.EstadoVisita;

public record VisitaRequest(
    @Positive Long clienteId,
    @NotNull @Positive Long propiedadId,
    @NotNull @Positive Long agenteId,
    @NotNull OffsetDateTime fechaHora,
    @Size(max = 1000) String observaciones,
    @NotNull EstadoVisita estado) {}
