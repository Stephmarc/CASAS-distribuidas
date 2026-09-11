package pe.edu.upeu.casaonada.visita.repository;
import pe.edu.upeu.casaonada.visita.domain.*; import org.springframework.data.jpa.repository.JpaRepository; import java.time.OffsetDateTime; import java.util.Collection;
public interface VisitaRepository extends JpaRepository<Visita,Long> { boolean existsByAgenteIdAndFechaHoraAndEstadoIn(Long agenteId,OffsetDateTime fechaHora,Collection<EstadoVisita> estados); }
