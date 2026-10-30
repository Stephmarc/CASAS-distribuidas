package pe.edu.upeu.casaonada.shared;

import java.time.OffsetDateTime;
import java.util.*;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/v1/operaciones/eventos")
public class EventOperations {
  private final JdbcTemplate db;
  private final ObjectMapper json;

  public EventOperations(JdbcTemplate d, ObjectMapper j) {
    db = d;
    json = j;
  }

  @KafkaListener(topics = Outbox.TOPIC + ".DLT", groupId = "${spring.application.name}-dlt")
  @Transactional
  public void archive(ConsumerRecord<String, String> r) {
    String id = r.topic() + ":" + r.partition() + ":" + r.offset();
    db.update(
        "insert into event_failures(failure_id,payload,created_at) values (?,?,?) on"
            + " conflict(failure_id) do nothing",
        id,
        r.value(),
        OffsetDateTime.now());
  }

  @GetMapping
  public Map<String, Object> status() {
    return Map.of(
        "pending",
        db.queryForObject(
            "select count(*) from event_outbox where published_at is null", Long.class),
        "published",
        db.queryForObject(
            "select count(*) from event_outbox where published_at is not null", Long.class),
        "processed",
        db.queryForObject("select count(*) from event_inbox", Long.class),
        "failed",
        db.queryForObject(
            "select count(*) from event_failures where replayed_at is null", Long.class));
  }

  @GetMapping("/fallidos")
  public List<Map<String, Object>> failures() {
    return db.queryForList(
        "select failure_id,payload,created_at,replayed_at from event_failures order by created_at"
            + " desc limit 100");
  }

  @PostMapping("/fallidos/{id}/reintentar")
  @Transactional
  public Map<String, Object> replay(@PathVariable String id) {
    var rows =
        db.queryForList("select payload from event_failures where failure_id=? for update", id);
    if (rows.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    var e = json.readValue(rows.get(0).get("payload").toString(), OperacionEvent.class);
    e.validate();
    db.update(
        "insert into event_outbox(event_id,aggregate_id,payload,created_at,attempts) values"
            + " (?,?,?,?,0) on conflict(event_id) do update set"
            + " published_at=null,last_error=null,next_attempt_at=null",
        e.eventId(),
        e.sagaId(),
        rows.get(0).get("payload").toString(),
        OffsetDateTime.now());
    db.update(
        "update event_failures set replayed_at=? where failure_id=?", OffsetDateTime.now(), id);
    return Map.of("requeued", e.eventId());
  }
}
