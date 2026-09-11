package pe.edu.upeu.casaonada.agente.mapper;
import org.springframework.stereotype.Component; import pe.edu.upeu.casaonada.agente.domain.Agente; import pe.edu.upeu.casaonada.agente.dto.*;
@Component public class AgenteMapper {
 public Agente toEntity(AgenteRequest r){ Agente e=new Agente(); update(e,r); return e; }
 public void update(Agente e,AgenteRequest r){
  e.setKeycloakUserId(r.keycloakUserId());
  e.setNombres(r.nombres());
  e.setApellidos(r.apellidos());
  e.setEmail(r.email());
  e.setTelefono(r.telefono());
  e.setMatricula(r.matricula());
  e.setEstado(r.estado());
 }
 public AgenteResponse toResponse(Agente e){ return new AgenteResponse(e.getId(), e.getKeycloakUserId(), e.getNombres(), e.getApellidos(), e.getEmail(), e.getTelefono(), e.getMatricula(), e.getEstado(), e.getCreatedAt(), e.getUpdatedAt()); }
}
