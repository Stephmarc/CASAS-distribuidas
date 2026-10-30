package pe.edu.upeu.casaonada.pago.service;

import java.io.*;
import java.security.MessageDigest;
import java.util.HexFormat;
import pe.edu.upeu.casaonada.pago.dto.PagoRequest;

public final class PaymentFingerprint {
  private PaymentFingerprint() {}

  public static String of(PagoRequest r) {
    try {
      var bytes = new ByteArrayOutputStream();
      var data = new DataOutputStream(bytes);
      for (String part :
          new String[] {
            r.ordenId().toString(),
            r.monto().stripTrailingZeros().toPlainString(),
            r.moneda(),
            r.descripcion(),
            r.payerEmail(),
            r.paymentMethodId(),
            r.token(),
            String.valueOf(r.installments() == null ? 1 : r.installments())
          }) {
        data.writeBoolean(part != null);
        if (part != null) {
          byte[] b = part.getBytes(java.nio.charset.StandardCharsets.UTF_8);
          data.writeInt(b.length);
          data.write(b);
        }
      }
      return HexFormat.of()
          .formatHex(MessageDigest.getInstance("SHA-256").digest(bytes.toByteArray()));
    } catch (Exception ex) {
      throw new IllegalStateException("No se pudo identificar la solicitud", ex);
    }
  }
}
