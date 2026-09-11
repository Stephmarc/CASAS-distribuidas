package pe.edu.upeu.casaonada.visita.service;
import pe.edu.upeu.casaonada.visita.domain.*; import pe.edu.upeu.casaonada.visita.dto.*; import pe.edu.upeu.casaonada.visita.exception.*; import pe.edu.upeu.casaonada.visita.mapper.VisitaMapper; import pe.edu.upeu.casaonada.visita.repository.VisitaRepository; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional; import java.time.OffsetDateTime; import java.util.*;
@Service public class VisitaService {
 private final VisitaRepository repo; private final VisitaMapper mapper; public VisitaService(VisitaRepository r,VisitaMapper m){repo=r;mapper=m;}
 @Transactional public VisitaResponse crear(VisitaRequest r){ if(r.fechaHora().isBefore(OffsetDateTime.now())) throw new BusinessRuleException("La visita debe programarse en una fecha futura"); if(repo.existsByAgenteIdAndFechaHoraAndEstadoIn(r.agenteId(),r.fechaHora(),List.of(EstadoVisita.SOLICITADA,EstadoVisita.CONFIRMADA,EstadoVisita.REPROGRAMADA))) throw new BusinessRuleException("El agente ya tiene una visita en ese horario"); return mapper.toResponse(repo.save(mapper.toEntity(r))); }
 @Transactional(readOnly=true) public List<VisitaResponse> listar(){return repo.findAll().stream().map(mapper::toResponse).toList();}
 @Transactional(readOnly=true) public VisitaResponse buscar(Long id){return mapper.toResponse(entidad(id));}
 @Transactional public VisitaResponse actualizar(Long id,VisitaRequest r){Visita e=entidad(id);mapper.update(e,r);return mapper.toResponse(repo.save(e));}
 @Transactional public VisitaResponse cambiarEstado(Long id,EstadoVisita estado){Visita e=entidad(id);e.setEstado(estado);return mapper.toResponse(repo.save(e));}
 @Transactional public void eliminar(Long id){repo.delete(entidad(id));}
 private Visita entidad(Long id){return repo.findById(id).orElseThrow(()->new ResourceNotFoundException("Visita no encontrada: "+id));}
}
