package pe.edu.upeu.casaonada.reserva.repository;
import pe.edu.upeu.casaonada.reserva.domain.*; import org.springframework.data.jpa.repository.JpaRepository; import java.util.Collection;
public interface ReservaRepository extends JpaRepository<Reserva,Long> { boolean existsByPropiedadIdAndEstadoIn(Long propiedadId, Collection<EstadoReserva> estados); }
