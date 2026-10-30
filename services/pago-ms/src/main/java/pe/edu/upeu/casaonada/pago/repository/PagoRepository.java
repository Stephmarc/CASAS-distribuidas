package pe.edu.upeu.casaonada.pago.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upeu.casaonada.pago.domain.Pago;

public interface PagoRepository extends JpaRepository<Pago, Long> {
  java.util.List<Pago> findByOrdenId(Long id);

  java.util.List<Pago> findByEstado(pe.edu.upeu.casaonada.pago.domain.EstadoPago estado);

  java.util.List<Pago> findByClienteId(Long id);

  java.util.List<Pago> findByEstadoIn(
      java.util.Collection<pe.edu.upeu.casaonada.pago.domain.EstadoPago> estados);

  Optional<Pago> findByIdempotencyKey(String key);
}
