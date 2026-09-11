package pe.edu.upeu.casaonada.reserva.service;
import pe.edu.upeu.casaonada.reserva.domain.*; import pe.edu.upeu.casaonada.reserva.dto.*; import pe.edu.upeu.casaonada.reserva.exception.*; import pe.edu.upeu.casaonada.reserva.mapper.ReservaMapper; import pe.edu.upeu.casaonada.reserva.repository.ReservaRepository; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional; import java.time.OffsetDateTime; import java.util.*;
@Service public class ReservaService {
 private final ReservaRepository repo; private final ReservaMapper mapper; public ReservaService(ReservaRepository r,ReservaMapper m){repo=r;mapper=m;}
 @Transactional public ReservaResponse crear(ReservaRequest req){
  if(repo.existsByPropiedadIdAndEstadoIn(req.propiedadId(),List.of(EstadoReserva.PENDIENTE,EstadoReserva.ACTIVA))) throw new BusinessRuleException("La propiedad ya tiene una reserva activa o pendiente");
  Reserva e=mapper.toEntity(req); if(e.getFechaExpiracion().isBefore(OffsetDateTime.now())) throw new BusinessRuleException("La fecha de expiración debe ser futura"); return mapper.toResponse(repo.save(e));
 }
 @Transactional(readOnly=true) public List<ReservaResponse> listar(){return repo.findAll().stream().map(mapper::toResponse).toList();}
 @Transactional(readOnly=true) public ReservaResponse buscar(Long id){return mapper.toResponse(entidad(id));}
 @Transactional public ReservaResponse actualizarEstado(Long id,EstadoReserva estado){ Reserva e=entidad(id); e.setEstado(estado); return mapper.toResponse(repo.save(e)); }
 @Transactional public void eliminar(Long id){repo.delete(entidad(id));}
 private Reserva entidad(Long id){return repo.findById(id).orElseThrow(()->new ResourceNotFoundException("Reserva no encontrada: "+id));}
}
