package pe.edu.upeu.casaonada.reserva.dto;
import java.time.*; import java.math.BigDecimal; import pe.edu.upeu.casaonada.reserva.domain.*;
public record ReservaRequest(@jakarta.validation.constraints.NotNull Long clienteId, @jakarta.validation.constraints.NotNull Long propiedadId, @jakarta.validation.constraints.NotNull OffsetDateTime fechaExpiracion, @jakarta.validation.constraints.NotNull @jakarta.validation.constraints.PositiveOrZero BigDecimal montoReserva, @jakarta.validation.constraints.NotNull EstadoReserva estado) {}
