package pe.edu.upeu.casaonada.pago.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "pago_comprobantes")
public class PagoComprobante {
  @Id
  @Column(name = "pago_id")
  public Long pagoId;

  @Column(nullable = false, length = 40)
  public String tipo;

  @Column(nullable = false)
  public byte[] datos;

  public PagoComprobante() {}
}
