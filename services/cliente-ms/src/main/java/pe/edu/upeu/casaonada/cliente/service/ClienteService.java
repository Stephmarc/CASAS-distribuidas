package pe.edu.upeu.casaonada.cliente.service;
import pe.edu.upeu.casaonada.cliente.domain.Cliente; import pe.edu.upeu.casaonada.cliente.dto.*; import pe.edu.upeu.casaonada.cliente.exception.ResourceNotFoundException; import pe.edu.upeu.casaonada.cliente.mapper.ClienteMapper; import pe.edu.upeu.casaonada.cliente.repository.ClienteRepository; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional; import java.util.List;
@Service public class ClienteService {
 private final ClienteRepository repo; private final ClienteMapper mapper; public ClienteService(ClienteRepository repo,ClienteMapper mapper){this.repo=repo;this.mapper=mapper;}
 @Transactional public ClienteResponse crear(ClienteRequest r){ return mapper.toResponse(repo.save(mapper.toEntity(r))); }
 @Transactional(readOnly=true) public List<ClienteResponse> listar(){ return repo.findAll().stream().map(mapper::toResponse).toList(); }
 @Transactional(readOnly=true) public ClienteResponse buscar(Long id){ return mapper.toResponse(entidad(id)); }
 @Transactional public ClienteResponse actualizar(Long id,ClienteRequest r){ Cliente e=entidad(id); mapper.update(e,r); return mapper.toResponse(repo.save(e)); }
 @Transactional public void eliminar(Long id){ repo.delete(entidad(id)); }
 private Cliente entidad(Long id){ return repo.findById(id).orElseThrow(()->new ResourceNotFoundException("Cliente no encontrado: "+id)); }
}
