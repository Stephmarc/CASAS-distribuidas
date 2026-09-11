package pe.edu.upeu.casaonada.cliente.domain;
import jakarta.persistence.*; import java.time.*; import java.math.BigDecimal;
@Entity @Table(name="clientes")
public class Cliente {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(name="keycloak_user_id",length=120,unique=true)
 private String keycloakUserId;
 @Column(nullable=false,length=100)
 private String nombres;
 @Column(nullable=false,length=100)
 private String apellidos;
 @Column(nullable=false,length=160,unique=true)
 private String email;
 @Column(length=30)
 private String telefono;
 @Column(name="presupuesto_min",precision=14,scale=2)
 private BigDecimal presupuestoMin;
 @Column(name="presupuesto_max",precision=14,scale=2)
 private BigDecimal presupuestoMax;
 @Column(name="ciudad_preferida",length=120)
 private String ciudadPreferida;
 @Column(name="tipo_operacion_preferida",length=20)
 private String tipoOperacionPreferida;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=20)
 private EstadoCliente estado;
 @Column(name="created_at",nullable=false,updatable=false) private OffsetDateTime createdAt; @Column(name="updated_at",nullable=false) private OffsetDateTime updatedAt;
 @PrePersist void prePersist(){ OffsetDateTime now=OffsetDateTime.now(); createdAt=now; updatedAt=now;  }
 @PreUpdate void preUpdate(){ updatedAt=OffsetDateTime.now(); }
 public Long getId(){return id;} public void setId(Long id){this.id=id;}
 public String getKeycloakUserId() { return keycloakUserId; }
 public String getNombres() { return nombres; }
 public String getApellidos() { return apellidos; }
 public String getEmail() { return email; }
 public String getTelefono() { return telefono; }
 public BigDecimal getPresupuestoMin() { return presupuestoMin; }
 public BigDecimal getPresupuestoMax() { return presupuestoMax; }
 public String getCiudadPreferida() { return ciudadPreferida; }
 public String getTipoOperacionPreferida() { return tipoOperacionPreferida; }
 public EstadoCliente getEstado() { return estado; }
 public void setKeycloakUserId(String keycloakUserId) { this.keycloakUserId=keycloakUserId; }
 public void setNombres(String nombres) { this.nombres=nombres; }
 public void setApellidos(String apellidos) { this.apellidos=apellidos; }
 public void setEmail(String email) { this.email=email; }
 public void setTelefono(String telefono) { this.telefono=telefono; }
 public void setPresupuestoMin(BigDecimal presupuestoMin) { this.presupuestoMin=presupuestoMin; }
 public void setPresupuestoMax(BigDecimal presupuestoMax) { this.presupuestoMax=presupuestoMax; }
 public void setCiudadPreferida(String ciudadPreferida) { this.ciudadPreferida=ciudadPreferida; }
 public void setTipoOperacionPreferida(String tipoOperacionPreferida) { this.tipoOperacionPreferida=tipoOperacionPreferida; }
 public void setEstado(EstadoCliente estado) { this.estado=estado; }
 public OffsetDateTime getCreatedAt(){return createdAt;} public OffsetDateTime getUpdatedAt(){return updatedAt;}
}
