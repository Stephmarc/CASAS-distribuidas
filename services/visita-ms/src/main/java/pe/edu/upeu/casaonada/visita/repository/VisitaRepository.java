package pe.edu.upeu.casaonada.visita.repository;

import java.time.OffsetDateTime;
import java.util.Collection;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upeu.casaonada.visita.domain.*;

public interface VisitaRepository extends JpaRepository<Visita, Long> {
  java.util.List<Visita> findByClienteId(Long id);

  java.util.List<Visita> findByFechaHoraAndEstadoIn(
      OffsetDateTime fecha, Collection<EstadoVisita> estados);

  @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
  @org.springframework.data.jpa.repository.Query("select v from Visita v where v.id=:id")
  java.util.Optional<Visita> locked(@org.springframework.data.repository.query.Param("id") Long id);

  boolean existsByAgenteIdAndFechaHoraAndEstadoIn(
      Long agenteId, OffsetDateTime fechaHora, Collection<EstadoVisita> estados);
}
