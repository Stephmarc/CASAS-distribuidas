package pe.edu.upeu.casaonada.orden.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
@Entity @Table(name="ordenes")
public class Orden {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @Column(nullable=false,unique=true) public Long reservaId;
 @Column(nullable=false) public Long propiedadId;
 @Column(nullable=false) public Long clienteId;
 @Column(nullable=false,precision=14,scale=2) public BigDecimal monto;
 @Column(nullable=false,precision=14,scale=2) public BigDecimal total;
 @Column(nullable=false,length=8) public String moneda;
 @Column(nullable=false,length=20) public String tipo;
 @Column(nullable=false,unique=true,length=36) public String sagaId;
 @Column(nullable=false,unique=true,length=120) public String idempotencyKey;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=40) public EstadoOrden estado;
 @Column(length=1000) public String motivo;
 @Column(length=80) public String traceId;
 @Column(nullable=false) public OffsetDateTime createdAt;
 @Column(nullable=false) public OffsetDateTime updatedAt;
 @Column(nullable=false) public OffsetDateTime deadline;
 @Column(nullable=false) public OffsetDateTime reservaExpiraAt;
 @Version public Long version;
 public Orden(){}
}
