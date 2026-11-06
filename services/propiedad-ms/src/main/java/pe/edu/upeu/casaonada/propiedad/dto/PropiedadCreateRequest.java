package pe.edu.upeu.casaonada.propiedad.dto;

import jakarta.validation.constraints.*;
import pe.edu.upeu.casaonada.propiedad.enums.TipoInmueble;
import pe.edu.upeu.casaonada.propiedad.enums.TipoOperacion;
import java.math.BigDecimal;

public record PropiedadCreateRequest(
    @NotBlank @Size(max = 150) String titulo,
    @Size(max = 5000) String descripcion,
    @NotNull TipoInmueble tipoInmueble,
    @NotNull TipoOperacion tipoOperacion,
    @NotNull @DecimalMin("0.01") @Digits(integer = 12, fraction = 2) BigDecimal precio,
    @NotBlank @Pattern(regexp = "PEN|USD") String moneda,
    @NotNull @DecimalMin("0.01") @Digits(integer = 8, fraction = 2) BigDecimal areaM2,
    @NotNull @Min(0) @Max(50) Integer dormitorios,
    @NotNull @Min(0) @Max(50) Integer banos,
    @NotBlank @Size(max = 250) String direccion,
    @NotBlank @Size(max = 100) String distrito,
    @NotBlank @Size(max = 100) String ciudad,
    @Positive Long propietarioId
) { }
