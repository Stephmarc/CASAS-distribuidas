package pe.edu.upeu.casaonada.propiedad.dto;
import java.time.*; import java.math.BigDecimal; import pe.edu.upeu.casaonada.propiedad.domain.*;
public record PropiedadResponse(Long id, String titulo, String descripcion, String ciudad, String direccion, BigDecimal precio, Integer habitaciones, Integer banos, BigDecimal areaM2, TipoOperacion tipoOperacion, EstadoPropiedad estado, Long agenteId, String imagenPrincipalUrl, OffsetDateTime createdAt, OffsetDateTime updatedAt) {}
