package pe.edu.upeu.casaonada.reserva.mapper;
import org.springframework.stereotype.Component; import pe.edu.upeu.casaonada.reserva.domain.Reserva; import pe.edu.upeu.casaonada.reserva.dto.*;
@Component public class ReservaMapper {
 public Reserva toEntity(ReservaRequest r){ Reserva e=new Reserva(); update(e,r); return e; }
 public void update(Reserva e,ReservaRequest r){
  e.setClienteId(r.clienteId());
  e.setPropiedadId(r.propiedadId());
  e.setFechaExpiracion(r.fechaExpiracion());
  e.setMontoReserva(r.montoReserva());
  e.setEstado(r.estado());
 }
 public ReservaResponse toResponse(Reserva e){ return new ReservaResponse(e.getId(), e.getClienteId(), e.getPropiedadId(), e.getFechaExpiracion(), e.getMontoReserva(), e.getEstado(), e.getCreatedAt(), e.getUpdatedAt()); }
}
