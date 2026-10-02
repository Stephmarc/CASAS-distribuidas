package pe.edu.upeu.casaonada.reserva.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import pe.edu.upeu.casaonada.reserva.domain.EstadoReserva;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * clienteId es opcional para CLIENTE: el controlador usa el claim cliente_id del JWT
 * y no confía en un identificador enviado por el navegador. Para ADMIN puede enviarse.
 */
public record ReservaRequest(
        Long clienteId,
        @NotNull Long propiedadId,
        @NotNull OffsetDateTime fechaExpiracion,
        @NotNull @PositiveOrZero BigDecimal montoReserva,
        @NotNull EstadoReserva estado
) {}
