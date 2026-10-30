package pe.edu.upeu.casaonada.reserva;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.*;
import org.junit.jupiter.api.Test;
import pe.edu.upeu.casaonada.reserva.domain.*;
import pe.edu.upeu.casaonada.reserva.repository.ReservaRepository;
import pe.edu.upeu.casaonada.reserva.service.ReservaExpiration;

class ReservaExpirationTest {
  @Test
  void noExpiraReservaAsociadaMientrasEsperabaElBloqueo() {
    var repo = mock(ReservaRepository.class);
    var em = mock(EntityManager.class);
    var r = new Reserva();
    r.setId(1L);
    r.setEstado(EstadoReserva.PENDIENTE);
    r.setFechaExpiracion(OffsetDateTime.now().minusHours(1));
    when(repo.findTop100ByFechaExpiracionBeforeAndSagaIdIsNullAndEstadoIn(any(), any()))
        .thenReturn(List.of(r));
    when(repo.locked(1L)).thenReturn(Optional.of(r));
    doAnswer(
            i -> {
              r.setSagaId("saga-concurrente");
              return null;
            })
        .when(em)
        .refresh(r, LockModeType.PESSIMISTIC_WRITE);
    new ReservaExpiration(repo, em).expire();
    assertEquals(EstadoReserva.PENDIENTE, r.getEstado());
    verify(repo, never()).save(any());
  }

  @Test
  void expiraReservaVencidaSinOrden() {
    var repo = mock(ReservaRepository.class);
    var em = mock(EntityManager.class);
    var r = new Reserva();
    r.setId(1L);
    r.setEstado(EstadoReserva.PENDIENTE);
    r.setFechaExpiracion(OffsetDateTime.now().minusHours(1));
    when(repo.findTop100ByFechaExpiracionBeforeAndSagaIdIsNullAndEstadoIn(any(), any()))
        .thenReturn(List.of(r));
    when(repo.locked(1L)).thenReturn(Optional.of(r));
    new ReservaExpiration(repo, em).expire();
    assertEquals(EstadoReserva.EXPIRADA, r.getEstado());
    verify(repo).save(r);
  }
}
