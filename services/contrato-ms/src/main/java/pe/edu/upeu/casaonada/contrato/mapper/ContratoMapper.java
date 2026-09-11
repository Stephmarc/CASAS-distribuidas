package pe.edu.upeu.casaonada.contrato.mapper;
import org.springframework.stereotype.Component; import pe.edu.upeu.casaonada.contrato.domain.Contrato; import pe.edu.upeu.casaonada.contrato.dto.*;
@Component public class ContratoMapper {
 public Contrato toEntity(ContratoRequest r){ Contrato e=new Contrato(); update(e,r); return e; }
 public void update(Contrato e,ContratoRequest r){
  e.setOrdenId(r.ordenId());
  e.setClienteId(r.clienteId());
  e.setPropiedadId(r.propiedadId());
  e.setTipo(r.tipo());
  e.setFechaInicio(r.fechaInicio());
  e.setFechaFin(r.fechaFin());
  e.setMonto(r.monto());
  e.setVersion(r.version());
  e.setEstado(r.estado());
 }
 public ContratoResponse toResponse(Contrato e){ return new ContratoResponse(e.getId(), e.getOrdenId(), e.getClienteId(), e.getPropiedadId(), e.getTipo(), e.getFechaInicio(), e.getFechaFin(), e.getMonto(), e.getVersion(), e.getEstado(), e.getCreatedAt(), e.getUpdatedAt()); }
}
