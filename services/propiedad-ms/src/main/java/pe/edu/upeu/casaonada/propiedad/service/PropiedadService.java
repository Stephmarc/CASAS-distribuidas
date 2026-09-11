package pe.edu.upeu.casaonada.propiedad.service;
import pe.edu.upeu.casaonada.propiedad.domain.*; import pe.edu.upeu.casaonada.propiedad.dto.*; import pe.edu.upeu.casaonada.propiedad.exception.ResourceNotFoundException; import pe.edu.upeu.casaonada.propiedad.mapper.PropiedadMapper; import pe.edu.upeu.casaonada.propiedad.repository.PropiedadRepository; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional; import java.math.BigDecimal; import java.util.List;
@Service public class PropiedadService {
 private final PropiedadRepository repo; private final PropiedadMapper mapper; public PropiedadService(PropiedadRepository r,PropiedadMapper m){repo=r;mapper=m;}
 @Transactional public PropiedadResponse crear(PropiedadRequest r){return mapper.toResponse(repo.save(mapper.toEntity(r)));}
 @Transactional(readOnly=true) public List<PropiedadResponse> listar(String ciudad,TipoOperacion tipo,EstadoPropiedad estado,BigDecimal min,BigDecimal max){return repo.buscar(vacioANull(ciudad),tipo,estado,min,max).stream().map(mapper::toResponse).toList();}
 @Transactional(readOnly=true) public PropiedadResponse buscar(Long id){return mapper.toResponse(entidad(id));}
 @Transactional public PropiedadResponse actualizar(Long id,PropiedadRequest r){Propiedad e=entidad(id);mapper.update(e,r);return mapper.toResponse(repo.save(e));}
 @Transactional public PropiedadResponse cambiarEstado(Long id,EstadoPropiedad estado){Propiedad e=entidad(id);e.setEstado(estado);return mapper.toResponse(repo.save(e));}
 @Transactional public void eliminar(Long id){repo.delete(entidad(id));}
 private Propiedad entidad(Long id){return repo.findById(id).orElseThrow(()->new ResourceNotFoundException("Propiedad no encontrada: "+id));}
 private String vacioANull(String s){return s==null||s.isBlank()?null:s;}
}
