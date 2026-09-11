package pe.edu.upeu.casaonada.visita.dto;
import java.time.*; import java.math.BigDecimal; import pe.edu.upeu.casaonada.visita.domain.*;
public record VisitaResponse(Long id, Long clienteId, Long propiedadId, Long agenteId, OffsetDateTime fechaHora, String observaciones, EstadoVisita estado, OffsetDateTime createdAt, OffsetDateTime updatedAt) {}
