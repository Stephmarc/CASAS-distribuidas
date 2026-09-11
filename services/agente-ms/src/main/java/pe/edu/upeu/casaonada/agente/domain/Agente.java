package pe.edu.upeu.casaonada.agente.domain;
import jakarta.persistence.*; import java.time.*; import java.math.BigDecimal;
@Entity @Table(name="agentes")
public class Agente {
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
 @Column(nullable=false,length=80,unique=true)
 private String matricula;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=20)
 private EstadoAgente estado;
 @Column(name="created_at",nullable=false,updatable=false) private OffsetDateTime createdAt; @Column(name="updated_at",nullable=false) private OffsetDateTime updatedAt;
 @PrePersist void prePersist(){ OffsetDateTime now=OffsetDateTime.now(); createdAt=now; updatedAt=now;  }
 @PreUpdate void preUpdate(){ updatedAt=OffsetDateTime.now(); }
 public Long getId(){return id;} public void setId(Long id){this.id=id;}
 public String getKeycloakUserId() { return keycloakUserId; }
 public String getNombres() { return nombres; }
 public String getApellidos() { return apellidos; }
 public String getEmail() { return email; }
 public String getTelefono() { return telefono; }
 public String getMatricula() { return matricula; }
 public EstadoAgente getEstado() { return estado; }
 public void setKeycloakUserId(String keycloakUserId) { this.keycloakUserId=keycloakUserId; }
 public void setNombres(String nombres) { this.nombres=nombres; }
 public void setApellidos(String apellidos) { this.apellidos=apellidos; }
 public void setEmail(String email) { this.email=email; }
 public void setTelefono(String telefono) { this.telefono=telefono; }
 public void setMatricula(String matricula) { this.matricula=matricula; }
 public void setEstado(EstadoAgente estado) { this.estado=estado; }
 public OffsetDateTime getCreatedAt(){return createdAt;} public OffsetDateTime getUpdatedAt(){return updatedAt;}
}
