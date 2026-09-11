package pe.edu.upeu.casaonada.propiedad.domain;
import jakarta.persistence.*; import java.time.*; import java.math.BigDecimal;
@Entity @Table(name="propiedades")
public class Propiedad {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false,length=160)
 private String titulo;
 @Column(nullable=false,length=2000)
 private String descripcion;
 @Column(nullable=false,length=120)
 private String ciudad;
 @Column(nullable=false,length=220)
 private String direccion;
 @Column(nullable=false,precision=14,scale=2)
 private BigDecimal precio;
 @Column(nullable=false)
 private Integer habitaciones;
 @Column(nullable=false)
 private Integer banos;
 @Column(name="area_m2",nullable=false,precision=10,scale=2)
 private BigDecimal areaM2;
 @Enumerated(EnumType.STRING) @Column(name="tipo_operacion",nullable=false,length=20)
 private TipoOperacion tipoOperacion;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=30)
 private EstadoPropiedad estado;
 @Column(name="agente_id")
 private Long agenteId;
 @Column(name="imagen_principal_url",length=500)
 private String imagenPrincipalUrl;
 @Column(name="created_at",nullable=false,updatable=false) private OffsetDateTime createdAt; @Column(name="updated_at",nullable=false) private OffsetDateTime updatedAt;
 @PrePersist void prePersist(){ OffsetDateTime now=OffsetDateTime.now(); createdAt=now; updatedAt=now;  }
 @PreUpdate void preUpdate(){ updatedAt=OffsetDateTime.now(); }
 public Long getId(){return id;} public void setId(Long id){this.id=id;}
 public String getTitulo() { return titulo; }
 public String getDescripcion() { return descripcion; }
 public String getCiudad() { return ciudad; }
 public String getDireccion() { return direccion; }
 public BigDecimal getPrecio() { return precio; }
 public Integer getHabitaciones() { return habitaciones; }
 public Integer getBanos() { return banos; }
 public BigDecimal getAreaM2() { return areaM2; }
 public TipoOperacion getTipoOperacion() { return tipoOperacion; }
 public EstadoPropiedad getEstado() { return estado; }
 public Long getAgenteId() { return agenteId; }
 public String getImagenPrincipalUrl() { return imagenPrincipalUrl; }
 public void setTitulo(String titulo) { this.titulo=titulo; }
 public void setDescripcion(String descripcion) { this.descripcion=descripcion; }
 public void setCiudad(String ciudad) { this.ciudad=ciudad; }
 public void setDireccion(String direccion) { this.direccion=direccion; }
 public void setPrecio(BigDecimal precio) { this.precio=precio; }
 public void setHabitaciones(Integer habitaciones) { this.habitaciones=habitaciones; }
 public void setBanos(Integer banos) { this.banos=banos; }
 public void setAreaM2(BigDecimal areaM2) { this.areaM2=areaM2; }
 public void setTipoOperacion(TipoOperacion tipoOperacion) { this.tipoOperacion=tipoOperacion; }
 public void setEstado(EstadoPropiedad estado) { this.estado=estado; }
 public void setAgenteId(Long agenteId) { this.agenteId=agenteId; }
 public void setImagenPrincipalUrl(String imagenPrincipalUrl) { this.imagenPrincipalUrl=imagenPrincipalUrl; }
 public OffsetDateTime getCreatedAt(){return createdAt;} public OffsetDateTime getUpdatedAt(){return updatedAt;}
}
