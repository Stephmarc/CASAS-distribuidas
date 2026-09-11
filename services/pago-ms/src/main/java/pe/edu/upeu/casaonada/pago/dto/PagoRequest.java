package pe.edu.upeu.casaonada.pago.dto;
import jakarta.validation.constraints.*; import java.math.BigDecimal;
public record PagoRequest(@NotNull Long ordenId,@NotNull @Positive BigDecimal monto,@NotBlank String moneda,@NotBlank String descripcion,@NotBlank @Email String payerEmail,@NotBlank String paymentMethodId,String token,@Min(1) Integer installments) {}
