package pe.edu.upeu.casaonada.reserva.service;

import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.casaonada.reserva.domain.EstadoReserva;
import pe.edu.upeu.casaonada.reserva.repository.ReservaRepository;

@Component
public class ReservaExpiration {
  private final jakarta.persistence.EntityManager em;
  private final ReservaRepository repo;

  public ReservaExpiration(ReservaRepository r, jakarta.persistence.EntityManager e) {
    em = e;
    repo = r;
  }

  @Scheduled(fixedDelay = 30000)
  @Transactional
  public void expire() {
    var now = OffsetDateTime.now();
    for (var candidate :
        repo.findTop100ByFechaExpiracionBeforeAndSagaIdIsNullAndEstadoIn(
            now, List.of(EstadoReserva.PENDIENTE, EstadoReserva.ACTIVA))) {
      var r = repo.locked(candidate.getId()).orElseThrow();
      em.refresh(r, jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
      if (r.getSagaId() == null
          && r.getFechaExpiracion().isBefore(now)
          && (r.getEstado() == EstadoReserva.PENDIENTE || r.getEstado() == EstadoReserva.ACTIVA)) {
        r.setEstado(EstadoReserva.EXPIRADA);
        repo.save(r);
      }
    }
  }
}
