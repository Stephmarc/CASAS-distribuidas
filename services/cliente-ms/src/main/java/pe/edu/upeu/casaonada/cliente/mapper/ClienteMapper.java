package pe.edu.upeu.casaonada.cliente.mapper;
import org.springframework.stereotype.Component; import pe.edu.upeu.casaonada.cliente.domain.Cliente; import pe.edu.upeu.casaonada.cliente.dto.*;
@Component public class ClienteMapper {
 public Cliente toEntity(ClienteRequest r){ Cliente e=new Cliente(); update(e,r); return e; }
 public void update(Cliente e,ClienteRequest r){
  e.setKeycloakUserId(r.keycloakUserId());
  e.setNombres(r.nombres());
  e.setApellidos(r.apellidos());
  e.setEmail(r.email());
  e.setTelefono(r.telefono());
  e.setPresupuestoMin(r.presupuestoMin());
  e.setPresupuestoMax(r.presupuestoMax());
  e.setCiudadPreferida(r.ciudadPreferida());
  e.setTipoOperacionPreferida(r.tipoOperacionPreferida());
  e.setEstado(r.estado());
 }
 public ClienteResponse toResponse(Cliente e){ return new ClienteResponse(e.getId(), e.getKeycloakUserId(), e.getNombres(), e.getApellidos(), e.getEmail(), e.getTelefono(), e.getPresupuestoMin(), e.getPresupuestoMax(), e.getCiudadPreferida(), e.getTipoOperacionPreferida(), e.getEstado(), e.getCreatedAt(), e.getUpdatedAt()); }
}
