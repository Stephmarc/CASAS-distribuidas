package pe.edu.upeu.casaonada.contrato.messaging;

import java.time.OffsetDateTime;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.casaonada.contrato.domain.*;
import pe.edu.upeu.casaonada.contrato.repository.ContratoRepository;
import pe.edu.upeu.casaonada.shared.*;
import tools.jackson.databind.ObjectMapper;

@Component
public class ContratoEvents {
  private final ContratoRepository repo;
  private final ObjectMapper json;
  private final Inbox inbox;
  private final Outbox outbox;
  private final JdbcTemplate db;

  public ContratoEvents(ContratoRepository r, ObjectMapper j, Inbox i, Outbox o, JdbcTemplate d) {
    repo = r;
    json = j;
    inbox = i;
    outbox = o;
    db = d;
  }

  @KafkaListener(topics = Outbox.TOPIC)
  @Transactional
  public void accept(String payload) {
    var e = json.readValue(payload, OperacionEvent.class);
    e.validate();
    if (!Set.of("CREAR_CONTRATO", "CANCELAR_CONTRATO").contains(e.type())) return;
    if (!inbox.first(e)) return;
    db.update(
        "insert into contrato_saga_guard(saga_id,cancelada) values (?,false) on conflict(saga_id)"
            + " do nothing",
        e.sagaId());
    boolean cancelled =
        Boolean.TRUE.equals(
            db.queryForObject(
                "select cancelada from contrato_saga_guard where saga_id=? for update",
                Boolean.class,
                e.sagaId()));
    var old = repo.findByOrdenId(e.ordenId());
    if (e.type().equals("CANCELAR_CONTRATO")) {
      db.update("update contrato_saga_guard set cancelada=true where saga_id=?", e.sagaId());
      old.ifPresent(
          c -> {
            c.setEstado(EstadoContrato.CANCELADO);
            repo.save(c);
          });
      outbox.add(e.next("CONTRATO_CANCELADO"));
      return;
    }
    if (cancelled
        || e.monto() == null
        || e.monto().signum() <= 0
        || e.total() == null
        || e.total().compareTo(e.monto()) < 0
        || !Set.of("COMPRA", "ALQUILER").contains(e.tipo())) {
      outbox.add(e.next("CONTRATO_RECHAZADO", "Contrato incompatible o saga cancelada"));
      return;
    }
    if (old.isPresent()) {
      var c = old.get();
      if (!c.getClienteId().equals(e.clienteId())
          || !c.getPropiedadId().equals(e.propiedadId())
          || !c.getTipo().name().equals(e.tipo())
          || c.getMonto().compareTo(e.total()) != 0
          || c.getEstado() == EstadoContrato.CANCELADO)
        throw new IllegalArgumentException("Contrato incompatible con la operacion");
    }
    if (old.isEmpty()) {
      var c = new Contrato();
      c.setOrdenId(e.ordenId());
      c.setClienteId(e.clienteId());
      c.setPropiedadId(e.propiedadId());
      c.setTipo(TipoContrato.valueOf(e.tipo()));
      c.setFechaInicio(OffsetDateTime.now());
      c.setMonto(e.total());
      c.setVersion(1);
      c.setEstado(EstadoContrato.GENERADO);
      repo.save(c);
    }
    outbox.add(e.next("CONTRATO_GENERADO"));
  }
}
