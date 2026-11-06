package pe.edu.upeu.casaonada.cliente.repository;

import pe.edu.upeu.casaonada.cliente.domain.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ClienteRepository extends JpaRepository<Cliente,Long>{
 boolean existsByDni(String dni);
 boolean existsByEmailIgnoreCase(String email);
 @org.springframework.data.jpa.repository.Query("select coalesce(max(c.id),0) from Cliente c") long maxId();
}
