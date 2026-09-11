package pe.edu.upeu.casaonada.visita.domain;
import jakarta.persistence.*; import java.time.*; import java.math.BigDecimal;
@Entity @Table(name="visitas")
public class Visita {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(name="cliente_id",nullable=false)
 private Long clienteId;
 @Column(name="propiedad_id",nullable=false)
 private Long propiedadId;
 @Column(name="agente_id",nullable=false)
 private Long agenteId;
 @Column(name="fecha_hora",nullable=false)
 private OffsetDateTime fechaHora;
 @Column(length=1000)
 private String observaciones;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=30)
 private EstadoVisita estado;
 @Column(name="created_at",nullable=false,updatable=false) private OffsetDateTime createdAt; @Column(name="updated_at",nullable=false) private OffsetDateTime updatedAt;
 @PrePersist void prePersist(){ OffsetDateTime now=OffsetDateTime.now(); createdAt=now; updatedAt=now;  }
 @PreUpdate void preUpdate(){ updatedAt=OffsetDateTime.now(); }
 public Long getId(){return id;} public void setId(Long id){this.id=id;}
 public Long getClienteId() { return clienteId; }
 public Long getPropiedadId() { return propiedadId; }
 public Long getAgenteId() { return agenteId; }
 public OffsetDateTime getFechaHora() { return fechaHora; }
 public String getObservaciones() { return observaciones; }
 public EstadoVisita getEstado() { return estado; }
 public void setClienteId(Long clienteId) { this.clienteId=clienteId; }
 public void setPropiedadId(Long propiedadId) { this.propiedadId=propiedadId; }
 public void setAgenteId(Long agenteId) { this.agenteId=agenteId; }
 public void setFechaHora(OffsetDateTime fechaHora) { this.fechaHora=fechaHora; }
 public void setObservaciones(String observaciones) { this.observaciones=observaciones; }
 public void setEstado(EstadoVisita estado) { this.estado=estado; }
 public OffsetDateTime getCreatedAt(){return createdAt;} public OffsetDateTime getUpdatedAt(){return updatedAt;}
}
