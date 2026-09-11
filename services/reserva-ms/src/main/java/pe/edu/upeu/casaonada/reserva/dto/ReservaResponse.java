package pe.edu.upeu.casaonada.reserva.dto;
import java.time.*; import java.math.BigDecimal; import pe.edu.upeu.casaonada.reserva.domain.*;
public record ReservaResponse(Long id, Long clienteId, Long propiedadId, OffsetDateTime fechaExpiracion, BigDecimal montoReserva, EstadoReserva estado, OffsetDateTime createdAt, OffsetDateTime updatedAt) {}
