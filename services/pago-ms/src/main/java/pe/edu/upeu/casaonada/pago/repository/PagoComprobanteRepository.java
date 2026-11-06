package pe.edu.upeu.casaonada.pago.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upeu.casaonada.pago.domain.PagoComprobante;

public interface PagoComprobanteRepository extends JpaRepository<PagoComprobante, Long> {}
