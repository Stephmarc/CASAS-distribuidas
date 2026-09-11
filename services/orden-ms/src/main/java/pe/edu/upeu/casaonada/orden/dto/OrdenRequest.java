package pe.edu.upeu.casaonada.orden.dto;
import pe.edu.upeu.casaonada.orden.domain.*; import jakarta.validation.Valid; import jakarta.validation.constraints.*; import java.math.BigDecimal; import java.util.List;
public record OrdenRequest(@NotNull Long clienteId,Long reservaId,@NotNull TipoOperacion tipoOperacion,@NotBlank String moneda,@NotNull @PositiveOrZero BigDecimal montoAdelanto,@NotEmpty List<@Valid DetalleOrdenRequest> detalles) {}
