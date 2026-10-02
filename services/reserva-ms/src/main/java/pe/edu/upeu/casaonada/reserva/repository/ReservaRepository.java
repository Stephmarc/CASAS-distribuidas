package pe.edu.upeu.casaonada.reserva.repository;

import pe.edu.upeu.casaonada.reserva.domain.EstadoReserva;
import pe.edu.upeu.casaonada.reserva.domain.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {
    boolean existsByPropiedadIdAndEstadoIn(Long propiedadId, Collection<EstadoReserva> estados);
    List<Reserva> findAllByClienteIdOrderByCreatedAtDesc(Long clienteId);
}
