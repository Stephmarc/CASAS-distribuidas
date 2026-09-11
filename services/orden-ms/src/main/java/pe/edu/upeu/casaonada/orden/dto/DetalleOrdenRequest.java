package pe.edu.upeu.casaonada.orden.dto;
import jakarta.validation.constraints.*; import java.math.BigDecimal;
public record DetalleOrdenRequest(@NotNull Long propiedadId,@NotBlank String concepto,@NotNull @Positive BigDecimal precioAcordado) {}
