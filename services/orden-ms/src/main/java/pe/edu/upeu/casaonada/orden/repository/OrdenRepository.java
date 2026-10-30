package pe.edu.upeu.casaonada.orden.repository;

import jakarta.persistence.LockModeType;
import java.time.OffsetDateTime;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import pe.edu.upeu.casaonada.orden.domain.*;

public interface OrdenRepository extends JpaRepository<Orden, Long> {
  Optional<Orden> findByIdempotencyKey(String key);

  List<Orden> findByClienteId(Long id);

  List<Orden> findTop100ByDeadlineBeforeAndEstadoNotIn(
      OffsetDateTime time, Collection<EstadoOrden> estados);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select o from Orden o where o.id=:id")
  Optional<Orden> locked(@Param("id") Long id);
}
