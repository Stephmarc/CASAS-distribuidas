package pe.edu.upeu.casaonada.visita.dto;
import java.time.*; import java.math.BigDecimal; import pe.edu.upeu.casaonada.visita.domain.*;
public record VisitaRequest(@jakarta.validation.constraints.NotNull Long clienteId, @jakarta.validation.constraints.NotNull Long propiedadId, @jakarta.validation.constraints.NotNull Long agenteId, @jakarta.validation.constraints.NotNull OffsetDateTime fechaHora, String observaciones, @jakarta.validation.constraints.NotNull EstadoVisita estado) {}
