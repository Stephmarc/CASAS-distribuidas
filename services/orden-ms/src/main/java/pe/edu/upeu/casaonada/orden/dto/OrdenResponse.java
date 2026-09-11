package pe.edu.upeu.casaonada.orden.dto;
import pe.edu.upeu.casaonada.orden.domain.*; import java.math.BigDecimal; import java.time.OffsetDateTime; import java.util.List;
public record OrdenResponse(Long id,Long clienteId,Long reservaId,TipoOperacion tipoOperacion,String moneda,BigDecimal subtotal,BigDecimal total,BigDecimal montoAdelanto,EstadoOrden estado,List<DetalleOrdenResponse> detalles,OffsetDateTime createdAt,OffsetDateTime updatedAt) {}
