package pe.edu.upeu.casaonada.reserva.messaging;

import java.time.OffsetDateTime;
import java.util.*;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.casaonada.reserva.domain.*;
import pe.edu.upeu.casaonada.reserva.repository.ReservaRepository;
import pe.edu.upeu.casaonada.shared.*;
import tools.jackson.databind.ObjectMapper;

@Component
public class ReservaEvents {
  private final org.springframework.jdbc.core.JdbcTemplate db;
  private final ReservaRepository repo;
  private final ObjectMapper json;
  private final Inbox inbox;
  private final Outbox outbox;

  public ReservaEvents(
      ReservaRepository r,
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
    if (!Set.of("ASOCIAR_RESERVA", "CONVERTIR_RESERVA", "CANCELAR_RESERVA").contains(e.type()))
      return;
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
    if (e.type().equals("CANCELAR_RESERVA"))
      db.update("update saga_fences set cancelada=true where saga_id=?", e.sagaId());
    else if (cancelled) {
      outbox.add(e.next("RESERVA_RECHAZADA", "Saga cancelada"));
      return;
    }
    var found = repo.locked(e.reservaId());
    if (found.isEmpty()) {
      outbox.add(
          e.next(
              e.type().equals("CANCELAR_RESERVA") ? "RESERVA_CANCELADA" : "RESERVA_RECHAZADA",
              "Reserva inexistente"));
      return;
    }
    var r = found.get();
    boolean owned = e.sagaId().equals(r.getSagaId());
    if (e.type().equals("ASOCIAR_RESERVA")) {
      if (owned && r.getEstado() == EstadoReserva.ACTIVA) {
        outbox.add(e.next("RESERVA_ASOCIADA"));
        return;
      }
      if (r.getSagaId() != null
          || !Set.of(EstadoReserva.ACTIVA, EstadoReserva.PENDIENTE).contains(r.getEstado())
          || !r.getFechaExpiracion().isAfter(OffsetDateTime.now())
          || !r.getClienteId().equals(e.clienteId())
          || !r.getPropiedadId().equals(e.propiedadId())
          || r.getMontoReserva().compareTo(e.monto()) != 0) {
        outbox.add(e.next("RESERVA_RECHAZADA", "Reserva no vigente o datos incompatibles"));
        return;
      }
      r.setSagaId(e.sagaId());
      r.setEstado(EstadoReserva.ACTIVA);
      repo.save(r);
      outbox.add(e.next("RESERVA_ASOCIADA"));
    } else if (e.type().equals("CONVERTIR_RESERVA")) {
      if (!owned
          || !Set.of(EstadoReserva.ACTIVA, EstadoReserva.CONVERTIDA).contains(r.getEstado())) {
        outbox.add(e.next("RESERVA_RECHAZADA", "La reserva no pertenece a la saga"));
        return;
      }
      r.setEstado(EstadoReserva.CONVERTIDA);
      repo.save(r);
      outbox.add(e.next("RESERVA_CONVERTIDA"));
    } else {
      if (owned
          || (r.getSagaId() == null
              && r.getClienteId().equals(e.clienteId())
              && r.getPropiedadId().equals(e.propiedadId())
              && Set.of(EstadoReserva.PENDIENTE, EstadoReserva.ACTIVA).contains(r.getEstado()))) {
        r.setSagaId(e.sagaId());
        r.setEstado(EstadoReserva.CANCELADA);
        repo.save(r);
      }
      outbox.add(e.next("RESERVA_CANCELADA"));
    }
  }
}
