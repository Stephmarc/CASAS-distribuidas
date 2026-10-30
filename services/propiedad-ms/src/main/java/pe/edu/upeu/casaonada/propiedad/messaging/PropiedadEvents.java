package pe.edu.upeu.casaonada.propiedad.messaging;

import java.util.*;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.casaonada.propiedad.domain.*;
import pe.edu.upeu.casaonada.propiedad.repository.PropiedadRepository;
import pe.edu.upeu.casaonada.shared.*;
import tools.jackson.databind.ObjectMapper;

@Component
public class PropiedadEvents {
  private final org.springframework.jdbc.core.JdbcTemplate db;
  private final PropiedadRepository repo;
  private final ObjectMapper json;
  private final Inbox inbox;
  private final Outbox outbox;

  public PropiedadEvents(
      PropiedadRepository r,
      ObjectMapper j,
      Inbox i,
      Outbox o,
      org.springframework.jdbc.core.JdbcTemplate d) {
    db = d;
    repo = r;
    json = j;
    inbox = i;
    outbox = o;
  }

  @KafkaListener(topics = Outbox.TOPIC)
  @Transactional
  public void accept(String payload) {
    var e = json.readValue(payload, OperacionEvent.class);
    e.validate();
    if (!Set.of("BLOQUEAR_PROPIEDAD", "CONFIRMAR_PROPIEDAD", "LIBERAR_PROPIEDAD")
        .contains(e.type())) return;
    if (!inbox.first(e)) return;
    db.update(
        "insert into saga_fences(saga_id,cancelada) values (?,false) on conflict(saga_id) do"
            + " nothing",
        e.sagaId());
    boolean cancelled =
        Boolean.TRUE.equals(
            db.queryForObject(
                "select cancelada from saga_fences where saga_id=? for update",
                Boolean.class,
                e.sagaId()));
    if (e.type().equals("LIBERAR_PROPIEDAD"))
      db.update("update saga_fences set cancelada=true where saga_id=?", e.sagaId());
    else if (cancelled) {
      outbox.add(e.next("PROPIEDAD_RECHAZADA", "Saga cancelada"));
      return;
    }
    var found = repo.locked(e.propiedadId());
    if (found.isEmpty()) {
      outbox.add(
          e.next(
              e.type().equals("LIBERAR_PROPIEDAD") ? "PROPIEDAD_LIBERADA" : "PROPIEDAD_RECHAZADA",
              "Propiedad inexistente"));
      return;
    }
    var p = found.get();
    boolean owned = e.sagaId().equals(p.getSagaId());
    switch (e.type()) {
      case "BLOQUEAR_PROPIEDAD" -> {
        if (owned && p.getEstado() == EstadoPropiedad.RESERVADA) {
          outbox.add(e.next("PROPIEDAD_BLOQUEADA"));
          return;
        }
        if (p.getEstado() != EstadoPropiedad.DISPONIBLE
            || p.getSagaId() != null
            || !p.getTipoOperacion().name().equals(e.tipo())
            || p.getPrecio().compareTo(e.total()) != 0) {
          outbox.add(e.next("PROPIEDAD_RECHAZADA", "Propiedad ocupada o tipo incompatible"));
          return;
        }
        p.setSagaId(e.sagaId());
        p.setEstado(EstadoPropiedad.RESERVADA);
        repo.save(p);
        outbox.add(e.next("PROPIEDAD_BLOQUEADA"));
      }
      case "CONFIRMAR_PROPIEDAD" -> {
        if (!owned) {
          outbox.add(e.next("PROPIEDAD_RECHAZADA", "La saga no posee el bloqueo"));
          return;
        }
        var target =
            TipoOperacion.COMPRA == p.getTipoOperacion()
                ? EstadoPropiedad.VENDIDA
                : EstadoPropiedad.ALQUILADA;
        if (p.getEstado() != EstadoPropiedad.RESERVADA && p.getEstado() != target) {
          outbox.add(e.next("PROPIEDAD_RECHAZADA", "Estado incompatible"));
          return;
        }
        p.setEstado(target);
        repo.save(p);
        outbox.add(e.next("PROPIEDAD_CONFIRMADA"));
      }
      case "LIBERAR_PROPIEDAD" -> {
        if (owned) {
          p.setEstado(EstadoPropiedad.DISPONIBLE);
          p.setSagaId(null);
          repo.save(p);
        }
        outbox.add(e.next("PROPIEDAD_LIBERADA"));
      }
    }
  }
}
