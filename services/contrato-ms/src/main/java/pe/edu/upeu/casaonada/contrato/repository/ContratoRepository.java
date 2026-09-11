package pe.edu.upeu.casaonada.contrato.repository;
import pe.edu.upeu.casaonada.contrato.domain.Contrato; import org.springframework.data.jpa.repository.JpaRepository;
public interface ContratoRepository extends JpaRepository<Contrato,Long> { boolean existsByOrdenId(Long ordenId); }
