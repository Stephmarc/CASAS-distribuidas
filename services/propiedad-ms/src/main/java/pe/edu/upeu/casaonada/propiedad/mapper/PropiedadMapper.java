package pe.edu.upeu.casaonada.propiedad.mapper;
import org.springframework.stereotype.Component; import pe.edu.upeu.casaonada.propiedad.domain.Propiedad; import pe.edu.upeu.casaonada.propiedad.dto.*;
@Component public class PropiedadMapper {
 public Propiedad toEntity(PropiedadRequest r){ Propiedad e=new Propiedad(); update(e,r); return e; }
 public void update(Propiedad e,PropiedadRequest r){
  e.setTitulo(r.titulo());
  e.setDescripcion(r.descripcion());
  e.setCiudad(r.ciudad());
  e.setDireccion(r.direccion());
  e.setPrecio(r.precio());
  e.setHabitaciones(r.habitaciones());
  e.setBanos(r.banos());
  e.setAreaM2(r.areaM2());
  e.setTipoOperacion(r.tipoOperacion());
  e.setEstado(r.estado());
  e.setAgenteId(r.agenteId());
  e.setImagenPrincipalUrl(r.imagenPrincipalUrl());
 }
 public PropiedadResponse toResponse(Propiedad e){ return new PropiedadResponse(e.getId(), e.getTitulo(), e.getDescripcion(), e.getCiudad(), e.getDireccion(), e.getPrecio(), e.getHabitaciones(), e.getBanos(), e.getAreaM2(), e.getTipoOperacion(), e.getEstado(), e.getAgenteId(), e.getImagenPrincipalUrl(), e.getCreatedAt(), e.getUpdatedAt()); }
}
