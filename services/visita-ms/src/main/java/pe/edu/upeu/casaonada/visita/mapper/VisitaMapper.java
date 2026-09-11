package pe.edu.upeu.casaonada.visita.mapper;
import org.springframework.stereotype.Component; import pe.edu.upeu.casaonada.visita.domain.Visita; import pe.edu.upeu.casaonada.visita.dto.*;
@Component public class VisitaMapper {
 public Visita toEntity(VisitaRequest r){ Visita e=new Visita(); update(e,r); return e; }
 public void update(Visita e,VisitaRequest r){
  e.setClienteId(r.clienteId());
  e.setPropiedadId(r.propiedadId());
  e.setAgenteId(r.agenteId());
  e.setFechaHora(r.fechaHora());
  e.setObservaciones(r.observaciones());
  e.setEstado(r.estado());
 }
 public VisitaResponse toResponse(Visita e){ return new VisitaResponse(e.getId(), e.getClienteId(), e.getPropiedadId(), e.getAgenteId(), e.getFechaHora(), e.getObservaciones(), e.getEstado(), e.getCreatedAt(), e.getUpdatedAt()); }
}
