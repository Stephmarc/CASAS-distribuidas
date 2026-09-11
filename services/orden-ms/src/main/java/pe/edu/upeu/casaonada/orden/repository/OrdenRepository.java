package pe.edu.upeu.casaonada.orden.repository;
import pe.edu.upeu.casaonada.orden.domain.Orden; import org.springframework.data.jpa.repository.*; import org.springframework.data.jpa.repository.EntityGraph; import java.util.*;
public interface OrdenRepository extends JpaRepository<Orden,Long> { @Override @EntityGraph(attributePaths="detalles") List<Orden> findAll(); @EntityGraph(attributePaths="detalles") Optional<Orden> findOneById(Long id); }
