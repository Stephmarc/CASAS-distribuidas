package pe.edu.upeu.casaonada.contrato.domain;
import jakarta.persistence.*; import java.time.*; import java.math.BigDecimal;
@Entity @Table(name="contratos")
public class Contrato {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(name="orden_id",nullable=false,unique=true)
 private Long ordenId;
 @Column(name="cliente_id",nullable=false)
 private Long clienteId;
 @Column(name="propiedad_id",nullable=false)
 private Long propiedadId;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=20)
 private TipoContrato tipo;
 @Column(name="fecha_inicio",nullable=false)
 private OffsetDateTime fechaInicio;
 @Column(name="fecha_fin")
 private OffsetDateTime fechaFin;
 @Column(nullable=false,precision=14,scale=2)
 private BigDecimal monto;
 @Column(nullable=false)
 private Integer version;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=30)
 private EstadoContrato estado;
 @Column(name="created_at",nullable=false,updatable=false) private OffsetDateTime createdAt; @Column(name="updated_at",nullable=false) private OffsetDateTime updatedAt;
 @PrePersist void prePersist(){ OffsetDateTime now=OffsetDateTime.now(); createdAt=now; updatedAt=now;  }
 @PreUpdate void preUpdate(){ updatedAt=OffsetDateTime.now(); }
 public Long getId(){return id;} public void setId(Long id){this.id=id;}
 public Long getOrdenId() { return ordenId; }
 public Long getClienteId() { return clienteId; }
 public Long getPropiedadId() { return propiedadId; }
 public TipoContrato getTipo() { return tipo; }
 public OffsetDateTime getFechaInicio() { return fechaInicio; }
 public OffsetDateTime getFechaFin() { return fechaFin; }
 public BigDecimal getMonto() { return monto; }
 public Integer getVersion() { return version; }
 public EstadoContrato getEstado() { return estado; }
 public void setOrdenId(Long ordenId) { this.ordenId=ordenId; }
 public void setClienteId(Long clienteId) { this.clienteId=clienteId; }
 public void setPropiedadId(Long propiedadId) { this.propiedadId=propiedadId; }
 public void setTipo(TipoContrato tipo) { this.tipo=tipo; }
 public void setFechaInicio(OffsetDateTime fechaInicio) { this.fechaInicio=fechaInicio; }
 public void setFechaFin(OffsetDateTime fechaFin) { this.fechaFin=fechaFin; }
 public void setMonto(BigDecimal monto) { this.monto=monto; }
 public void setVersion(Integer version) { this.version=version; }
 public void setEstado(EstadoContrato estado) { this.estado=estado; }
 public OffsetDateTime getCreatedAt(){return createdAt;} public OffsetDateTime getUpdatedAt(){return updatedAt;}
}
