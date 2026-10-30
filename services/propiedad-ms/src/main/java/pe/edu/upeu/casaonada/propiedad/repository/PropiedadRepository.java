package pe.edu.upeu.casaonada.propiedad.repository;
import pe.edu.upeu.casaonada.propiedad.domain.*; import org.springframework.data.jpa.repository.*; import org.springframework.data.repository.query.Param; import java.math.BigDecimal; import java.util.*; import jakarta.persistence.criteria.Predicate;
public interface PropiedadRepository extends JpaRepository<Propiedad,Long>, JpaSpecificationExecutor<Propiedad> {
 @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
 @org.springframework.data.jpa.repository.Query("select e from Propiedad e where e.id=:id")
 java.util.Optional<Propiedad> locked(@org.springframework.data.repository.query.Param("id") Long id);
 default List<Propiedad> buscar(String ciudad, TipoOperacion tipo, EstadoPropiedad estado, BigDecimal min, BigDecimal max) {
   return findAll((root, query, cb) -> {
     var filters = new ArrayList<Predicate>();
     if (ciudad != null) filters.add(cb.equal(cb.lower(root.get("ciudad")), ciudad.toLowerCase(Locale.ROOT)));
     if (tipo != null) filters.add(cb.equal(root.get("tipoOperacion"), tipo));
     if (estado != null) filters.add(cb.equal(root.get("estado"), estado));
     if (min != null) filters.add(cb.greaterThanOrEqualTo(root.get("precio"), min));
     if (max != null) filters.add(cb.lessThanOrEqualTo(root.get("precio"), max));
     query.orderBy(cb.desc(root.get("createdAt")));
     return cb.and(filters.toArray(Predicate[]::new));
   });
 }
}
