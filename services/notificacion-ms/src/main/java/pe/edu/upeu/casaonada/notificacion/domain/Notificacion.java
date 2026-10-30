package pe.edu.upeu.casaonada.notificacion.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
@Entity @Table(name="notificaciones") public class Notificacion {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @Column(nullable=false,unique=true,length=36) public String eventId;
 @Column(nullable=false) public Long clienteId;
 @Column(nullable=false) public Long ordenId;
 @Column(nullable=false,length=40) public String tipo;
 @Column(nullable=false,length=1000) public String mensaje;
 @Column(nullable=false) public OffsetDateTime createdAt;
 public boolean leida;
}
