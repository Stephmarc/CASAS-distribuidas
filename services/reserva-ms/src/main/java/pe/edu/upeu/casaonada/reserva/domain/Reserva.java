package pe.edu.upeu.casaonada.reserva.domain;
import jakarta.persistence.*; import java.time.*; import java.math.BigDecimal;
@Entity @Table(name="reservas")
public class Reserva {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(name="cliente_id",nullable=false)
 private Long clienteId;
 @Column(name="propiedad_id",nullable=false)
 private Long propiedadId;
 @Column(name="fecha_expiracion",nullable=false)
 private OffsetDateTime fechaExpiracion;
 @Column(name="monto_reserva",nullable=false,precision=14,scale=2)
 private BigDecimal montoReserva;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=30)
 private EstadoReserva estado;
 @Column(name="created_at",nullable=false,updatable=false) private OffsetDateTime createdAt; @Column(name="updated_at",nullable=false) private OffsetDateTime updatedAt;
 @PrePersist void prePersist(){ OffsetDateTime now=OffsetDateTime.now(); createdAt=now; updatedAt=now;  }
 @PreUpdate void preUpdate(){ updatedAt=OffsetDateTime.now(); }
 public Long getId(){return id;} public void setId(Long id){this.id=id;}
 public Long getClienteId() { return clienteId; }
 public Long getPropiedadId() { return propiedadId; }
 public OffsetDateTime getFechaExpiracion() { return fechaExpiracion; }
 public BigDecimal getMontoReserva() { return montoReserva; }
 public EstadoReserva getEstado() { return estado; }
 public void setClienteId(Long clienteId) { this.clienteId=clienteId; }
 public void setPropiedadId(Long propiedadId) { this.propiedadId=propiedadId; }
 public void setFechaExpiracion(OffsetDateTime fechaExpiracion) { this.fechaExpiracion=fechaExpiracion; }
 public void setMontoReserva(BigDecimal montoReserva) { this.montoReserva=montoReserva; }
 public void setEstado(EstadoReserva estado) { this.estado=estado; }
 public OffsetDateTime getCreatedAt(){return createdAt;} public OffsetDateTime getUpdatedAt(){return updatedAt;}
}
