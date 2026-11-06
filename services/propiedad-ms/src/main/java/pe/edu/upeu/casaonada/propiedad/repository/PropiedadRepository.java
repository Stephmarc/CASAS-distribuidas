package pe.edu.upeu.casaonada.propiedad.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import pe.edu.upeu.casaonada.propiedad.entity.Propiedad;

public interface PropiedadRepository extends JpaRepository<Propiedad, Long>,
        JpaSpecificationExecutor<Propiedad> { }
