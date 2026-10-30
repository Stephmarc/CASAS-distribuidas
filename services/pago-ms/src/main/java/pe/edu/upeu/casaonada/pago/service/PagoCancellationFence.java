package pe.edu.upeu.casaonada.pago.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
import pe.edu.upeu.casaonada.shared.OperacionEvent;

@Service
public class PagoCancellationFence {
  private final PagoSagaGuard guard;

  public PagoCancellationFence(PagoSagaGuard g) {
    guard = g;
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void close(OperacionEvent e) {
    guard.lock(e);
    guard.cancel(e);
  }
}
