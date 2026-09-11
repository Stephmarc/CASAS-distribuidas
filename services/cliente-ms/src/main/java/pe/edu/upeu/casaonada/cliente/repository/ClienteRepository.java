package pe.edu.upeu.casaonada.cliente.repository;
import pe.edu.upeu.casaonada.cliente.domain.Cliente; import org.springframework.data.jpa.repository.JpaRepository;
public interface ClienteRepository extends JpaRepository<Cliente,Long> {}
