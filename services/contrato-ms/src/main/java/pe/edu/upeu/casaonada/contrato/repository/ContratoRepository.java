package pe.edu.upeu.casaonada.contrato.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upeu.casaonada.contrato.domain.Contrato;

public interface ContratoRepository extends JpaRepository<Contrato, Long> {
  @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
  @org.springframework.data.jpa.repository.Query("select c from Contrato c where c.id=:id")
  java.util.Optional<Contrato> locked(
      @org.springframework.data.repository.query.Param("id") Long id);

  java.util.Optional<Contrato> findByOrdenId(Long ordenId);

  boolean existsByOrdenId(Long ordenId);
}
