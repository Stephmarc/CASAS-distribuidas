package pe.edu.upeu.casaonada.propiedad.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import pe.edu.upeu.casaonada.propiedad.domain.*;

public record PropiedadRequest(
    @NotBlank @Size(max = 160) String titulo,
    @NotBlank @Size(max = 2000) String descripcion,
    @NotBlank @Size(max = 120) String ciudad,
    @NotBlank @Size(max = 220) String direccion,
    @NotNull @Positive @Digits(integer = 12, fraction = 2) BigDecimal precio,
    @NotNull @PositiveOrZero Integer habitaciones,
    @NotNull @PositiveOrZero Integer banos,
    @NotNull @Positive @Digits(integer = 8, fraction = 2) BigDecimal areaM2,
    @NotNull TipoOperacion tipoOperacion,
    @NotNull EstadoPropiedad estado,
    @Positive Long agenteId,
    @Size(max = 500) String imagenPrincipalUrl) {}
