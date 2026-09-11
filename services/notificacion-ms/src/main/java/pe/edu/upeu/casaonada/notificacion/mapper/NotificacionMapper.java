package pe.edu.upeu.casaonada.notificacion.mapper;
import org.springframework.stereotype.Component; import pe.edu.upeu.casaonada.notificacion.domain.Notificacion; import pe.edu.upeu.casaonada.notificacion.dto.*;
@Component public class NotificacionMapper {
 public Notificacion toEntity(NotificacionRequest r){ Notificacion e=new Notificacion(); update(e,r); return e; }
 public void update(Notificacion e,NotificacionRequest r){
  e.setDestinatario(r.destinatario());
  e.setTipo(r.tipo());
  e.setAsunto(r.asunto());
  e.setContenido(r.contenido());
  e.setEstado(r.estado());
 }
 public NotificacionResponse toResponse(Notificacion e){ return new NotificacionResponse(e.getId(), e.getDestinatario(), e.getTipo(), e.getAsunto(), e.getContenido(), e.getEstado(), e.getCreatedAt(), e.getUpdatedAt()); }
}
