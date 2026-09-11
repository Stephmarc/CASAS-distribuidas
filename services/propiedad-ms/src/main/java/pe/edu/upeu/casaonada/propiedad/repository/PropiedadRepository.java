package pe.edu.upeu.casaonada.propiedad.repository;
import pe.edu.upeu.casaonada.propiedad.domain.*; import org.springframework.data.jpa.repository.*; import org.springframework.data.repository.query.Param; import java.math.BigDecimal; import java.util.List;
public interface PropiedadRepository extends JpaRepository<Propiedad,Long> {
 @Query("select p from Propiedad p where (:ciudad is null or lower(p.ciudad)=lower(:ciudad)) and (:tipo is null or p.tipoOperacion=:tipo) and (:estado is null or p.estado=:estado) and (:min is null or p.precio>=:min) and (:max is null or p.precio<=:max) order by p.createdAt desc")
 List<Propiedad> buscar(@Param("ciudad") String ciudad,@Param("tipo") TipoOperacion tipo,@Param("estado") EstadoPropiedad estado,@Param("min") BigDecimal min,@Param("max") BigDecimal max);
}
