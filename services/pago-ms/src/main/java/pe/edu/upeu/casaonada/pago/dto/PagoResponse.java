package pe.edu.upeu.casaonada.pago.dto;
import pe.edu.upeu.casaonada.pago.domain.EstadoPago; import java.math.BigDecimal; import java.time.OffsetDateTime;
public record PagoResponse(Long id,Long ordenId,BigDecimal monto,String moneda,String proveedor,String providerPaymentId,String idempotencyKey,EstadoPago estado,OffsetDateTime createdAt,OffsetDateTime updatedAt) {}
