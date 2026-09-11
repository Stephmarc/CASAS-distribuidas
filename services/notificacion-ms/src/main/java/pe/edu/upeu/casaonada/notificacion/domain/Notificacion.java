package pe.edu.upeu.casaonada.notificacion.domain;
import jakarta.persistence.*; import java.time.*; import java.math.BigDecimal;
@Entity @Table(name="notificaciones")
public class Notificacion {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false,length=180)
 private String destinatario;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=30)
 private TipoNotificacion tipo;
 @Column(nullable=false,length=180)
 private String asunto;
 @Column(nullable=false,length=3000)
 private String contenido;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=20)
 private EstadoNotificacion estado;
 @Column(name="created_at",nullable=false,updatable=false) private OffsetDateTime createdAt; @Column(name="updated_at",nullable=false) private OffsetDateTime updatedAt;
 @PrePersist void prePersist(){ OffsetDateTime now=OffsetDateTime.now(); createdAt=now; updatedAt=now;  }
 @PreUpdate void preUpdate(){ updatedAt=OffsetDateTime.now(); }
 public Long getId(){return id;} public void setId(Long id){this.id=id;}
 public String getDestinatario() { return destinatario; }
 public TipoNotificacion getTipo() { return tipo; }
 public String getAsunto() { return asunto; }
 public String getContenido() { return contenido; }
 public EstadoNotificacion getEstado() { return estado; }
 public void setDestinatario(String destinatario) { this.destinatario=destinatario; }
 public void setTipo(TipoNotificacion tipo) { this.tipo=tipo; }
 public void setAsunto(String asunto) { this.asunto=asunto; }
 public void setContenido(String contenido) { this.contenido=contenido; }
 public void setEstado(EstadoNotificacion estado) { this.estado=estado; }
 public OffsetDateTime getCreatedAt(){return createdAt;} public OffsetDateTime getUpdatedAt(){return updatedAt;}
}
