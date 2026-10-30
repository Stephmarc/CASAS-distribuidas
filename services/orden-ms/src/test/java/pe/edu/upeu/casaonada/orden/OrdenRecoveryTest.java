package pe.edu.upeu.casaonada.orden;

import static org.mockito.Mockito.*;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.*;
import org.junit.jupiter.api.Test;
import pe.edu.upeu.casaonada.orden.domain.*;
import pe.edu.upeu.casaonada.orden.messaging.OrdenEvents;
import pe.edu.upeu.casaonada.orden.repository.OrdenRepository;
import pe.edu.upeu.casaonada.orden.service.OrdenService;
import pe.edu.upeu.casaonada.shared.*;
import tools.jackson.databind.ObjectMapper;

class OrdenRecoveryTest {
  @Test
  void noCompensaUnaOrdenConfirmadaMientrasEsperabaElBloqueo() {
    check(true);
  }

  @Test
  void noCompensaSiLaEtapaYaRenovoSuPlazo() {
    check(false);
  }

  void check(boolean confirmed) {
    var repo = mock(OrdenRepository.class);
    var service = mock(OrdenService.class);
    var outbox = mock(Outbox.class);
    var em = mock(EntityManager.class);
    var o = new Orden();
    o.id = 1L;
    o.estado = EstadoOrden.ESPERANDO_PAGO;
    o.deadline = OffsetDateTime.now().minusMinutes(1);
    when(repo.findTop100ByDeadlineBeforeAndEstadoNotIn(any(), any())).thenReturn(List.of(o));
    when(repo.locked(1L)).thenReturn(Optional.of(o));
    doAnswer(
            i -> {
              if (confirmed) o.estado = EstadoOrden.CONFIRMADA;
              else o.deadline = OffsetDateTime.now().plusMinutes(30);
              return null;
            })
        .when(em)
        .refresh(o, LockModeType.PESSIMISTIC_WRITE);
    new OrdenEvents(mock(Inbox.class), outbox, mock(ObjectMapper.class), repo, service, em)
        .recover();
    verifyNoInteractions(service, outbox);
  }
}
