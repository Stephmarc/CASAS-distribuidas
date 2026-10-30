package pe.edu.upeu.casaonada.propiedad;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.*;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.springframework.jdbc.core.JdbcTemplate;
import pe.edu.upeu.casaonada.propiedad.domain.*;
import pe.edu.upeu.casaonada.propiedad.messaging.PropiedadEvents;
import pe.edu.upeu.casaonada.propiedad.repository.PropiedadRepository;
import pe.edu.upeu.casaonada.shared.*;
import tools.jackson.databind.ObjectMapper;

class PropiedadEventsTest {
  PropiedadRepository repo;
  ObjectMapper json;
  Inbox inbox;
  Outbox outbox;
  JdbcTemplate db;
  PropiedadEvents listener;
  Propiedad p;

  OperacionEvent event(String type) {
    return new OperacionEvent(
        UUID.randomUUID().toString(),
        "saga-1",
        type,
        1L,
        1L,
        1L,
        1L,
        new BigDecimal("500"),
        "PEN",
        "COMPRA",
        "trace",
        null,
        new BigDecimal("100000"));
  }

  @BeforeEach
  void setup() {
    repo = mock(PropiedadRepository.class);
    json = new ObjectMapper();
    inbox = mock(Inbox.class);
    outbox = mock(Outbox.class);
    db = mock(JdbcTemplate.class);
    listener = new PropiedadEvents(repo, json, inbox, outbox, db);
    p = new Propiedad();
    p.setId(1L);
    p.setPrecio(new BigDecimal("100000"));
    p.setEstado(EstadoPropiedad.DISPONIBLE);
    p.setTipoOperacion(TipoOperacion.COMPRA);
    when(inbox.first(any())).thenReturn(true);
    when(repo.locked(1L)).thenReturn(Optional.of(p));
  }

  @Test
  void bloqueaPropiedadDisponible() {
    listener.accept(json.writeValueAsString(event("BLOQUEAR_PROPIEDAD")));
    assertEquals(EstadoPropiedad.RESERVADA, p.getEstado());
    assertEquals("saga-1", p.getSagaId());
  }

  @Test
  void noRobaBloqueoDeOtraSaga() {
    p.setSagaId("saga-otra");
    p.setEstado(EstadoPropiedad.RESERVADA);
    listener.accept(json.writeValueAsString(event("BLOQUEAR_PROPIEDAD")));
    assertEquals("saga-otra", p.getSagaId());
    var c = ArgumentCaptor.forClass(OperacionEvent.class);
    verify(outbox).add(c.capture());
    assertEquals("PROPIEDAD_RECHAZADA", c.getValue().type());
  }

  @Test
  void noLiberaPropiedadAjena() {
    p.setSagaId("saga-otra");
    p.setEstado(EstadoPropiedad.RESERVADA);
    listener.accept(json.writeValueAsString(event("LIBERAR_PROPIEDAD")));
    assertEquals(EstadoPropiedad.RESERVADA, p.getEstado());
  }

  @Test
  void compensacionRevierteVentaDeSuSaga() {
    p.setSagaId("saga-1");
    p.setEstado(EstadoPropiedad.VENDIDA);
    listener.accept(json.writeValueAsString(event("LIBERAR_PROPIEDAD")));
    assertEquals(EstadoPropiedad.DISPONIBLE, p.getEstado());
    assertNull(p.getSagaId());
  }

  @Test
  void duplicadoNoMuta() {
    when(inbox.first(any())).thenReturn(false);
    listener.accept(json.writeValueAsString(event("BLOQUEAR_PROPIEDAD")));
    verifyNoInteractions(repo);
    verifyNoInteractions(outbox);
  }
}
