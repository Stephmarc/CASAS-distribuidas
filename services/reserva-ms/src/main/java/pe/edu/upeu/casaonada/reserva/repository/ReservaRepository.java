package pe.edu.upeu.casaonada.reserva.repository;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upeu.casaonada.reserva.domain.EstadoReserva;
import pe.edu.upeu.casaonada.reserva.domain.Reserva;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {
  @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
  @org.springframework.data.jpa.repository.Query("select e from Reserva e where e.id=:id")
  java.util.Optional<Reserva> locked(
      @org.springframework.data.repository.query.Param("id") Long id);

  List<Reserva> findTop100ByFechaExpiracionBeforeAndSagaIdIsNullAndEstadoIn(
      java.time.OffsetDateTime time, Collection<EstadoReserva> estados);

  boolean existsByPropiedadIdAndEstadoIn(Long propiedadId, Collection<EstadoReserva> estados);

  List<Reserva> findAllByClienteIdOrderByCreatedAtDesc(Long clienteId);
}
