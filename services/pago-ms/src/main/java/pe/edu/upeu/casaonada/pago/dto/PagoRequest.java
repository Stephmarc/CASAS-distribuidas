package pe.edu.upeu.casaonada.pago.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record PagoRequest(
    @NotNull @Positive Long ordenId,
    @NotNull @Positive @Digits(integer = 12, fraction = 2) BigDecimal monto,
    @NotBlank @Pattern(regexp = "PEN") String moneda,
    @NotBlank @Size(max = 500) String descripcion,
    @NotBlank @Email @Size(max = 200) String payerEmail,
    @NotBlank @Size(max = 80) String paymentMethodId,
    @Size(max = 1000) String token,
    @Min(1) @Max(48) Integer installments) {}
