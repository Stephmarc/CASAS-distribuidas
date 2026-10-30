package pe.edu.upeu.casaonada.notificacion.repository;

import pe.edu.upeu.casaonada.notificacion.domain.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface NotificacionRepository extends JpaRepository<Notificacion,Long>{List<Notificacion> findByClienteIdOrderByCreatedAtDesc(Long id);}
