package pe.edu.upeu.casaonada.agente.service;
import pe.edu.upeu.casaonada.agente.domain.Agente; import pe.edu.upeu.casaonada.agente.dto.*; import pe.edu.upeu.casaonada.agente.exception.ResourceNotFoundException; import pe.edu.upeu.casaonada.agente.mapper.AgenteMapper; import pe.edu.upeu.casaonada.agente.repository.AgenteRepository; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional; import java.util.List;
@Service public class AgenteService {
 private final AgenteRepository repo; private final AgenteMapper mapper; public AgenteService(AgenteRepository repo,AgenteMapper mapper){this.repo=repo;this.mapper=mapper;}
 @Transactional public AgenteResponse crear(AgenteRequest r){ return mapper.toResponse(repo.save(mapper.toEntity(r))); }
 @Transactional(readOnly=true) public List<AgenteResponse> listar(){ return repo.findAll().stream().map(mapper::toResponse).toList(); }
 @Transactional(readOnly=true) public AgenteResponse buscar(Long id){ return mapper.toResponse(entidad(id)); }
 @Transactional public AgenteResponse actualizar(Long id,AgenteRequest r){ Agente e=entidad(id); mapper.update(e,r); return mapper.toResponse(repo.save(e)); }
 @Transactional public void eliminar(Long id){ repo.delete(entidad(id)); }
 private Agente entidad(Long id){ return repo.findById(id).orElseThrow(()->new ResourceNotFoundException("Agente no encontrado: "+id)); }
}
