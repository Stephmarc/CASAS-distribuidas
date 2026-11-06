package pe.edu.upeu.casaonada.propiedad.dto;

import pe.edu.upeu.casaonada.propiedad.enums.EstadoPropiedad;
import pe.edu.upeu.casaonada.propiedad.enums.TipoInmueble;
import pe.edu.upeu.casaonada.propiedad.enums.TipoOperacion;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PropiedadResponse(
    Long id,
    String titulo,
    String descripcion,
    TipoInmueble tipoInmueble,
    TipoOperacion tipoOperacion,
    BigDecimal precio,
    String moneda,
    BigDecimal areaM2,
    Integer dormitorios,
    Integer banos,
    String direccion,
    String distrito,
    String ciudad,
    EstadoPropiedad estado,
    Long propietarioId,
    Long version,
    LocalDateTime fechaCreacion,
    LocalDateTime fechaActualizacion
) { }
