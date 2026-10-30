package pe.edu.upeu.casaonada.shared;

import java.time.OffsetDateTime;
import java.util.concurrent.TimeUnit;
import org.slf4j.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.*;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.ObjectMapper;

@Component
public class Outbox {
  public static final String TOPIC = "casa.operaciones.v1";
  private static final Logger log = LoggerFactory.getLogger(Outbox.class);
  private final JdbcTemplate db;
  private final KafkaTemplate<String, String> kafka;
  private final ObjectMapper json;
  private final TransactionTemplate tx;

  public Outbox(
      JdbcTemplate db,
      KafkaTemplate<String, String> kafka,
      ObjectMapper json,
      org.springframework.transaction.PlatformTransactionManager manager) {
    this.db = db;
    this.kafka = kafka;
    this.json = json;
    this.tx = new TransactionTemplate(manager);
  }

  @Transactional(propagation = Propagation.MANDATORY)
  public void add(OperacionEvent e) {
    e.validate();
    log.info("OUTBOX type={} eventId={} sagaId={} traceId={}", e.type(),e.eventId(),e.sagaId(),e.traceId());
    db.update(
        "insert into event_outbox(event_id,aggregate_id,payload,created_at,attempts)"
            + " values(?,?,?,?,0)",
        e.eventId(),
        e.sagaId(),
        json.writeValueAsString(e),
        OffsetDateTime.now());
  }

  @Scheduled(fixedDelayString = "${app.outbox.delay-ms:1000}")
  public void publish() {
    for (int i = 0; i < 20; i++) {
      if (!Boolean.TRUE.equals(tx.execute(status -> publishOne()))) break;
    }
  }

  private boolean publishOne() {
    var rows =
        db.queryForList(
            "select o.event_id,o.aggregate_id,o.payload,o.attempts from event_outbox o where"
                + " o.published_at is null and (o.next_attempt_at is null or"
                + " o.next_attempt_at<=now()) and not exists(select 1 from event_outbox prior where"
                + " prior.aggregate_id=o.aggregate_id and prior.published_at is null and"
                + " prior.dispatch_seq<o.dispatch_seq) order by o.dispatch_seq limit 1 for update"
                + " of o skip locked");
    if (rows.isEmpty()) return false;
    var r = rows.get(0);
    try {
      kafka
          .send(TOPIC, r.get("aggregate_id").toString(), r.get("payload").toString())
          .get(12, TimeUnit.SECONDS);
      db.update(
          "update event_outbox set"
              + " published_at=?,attempts=attempts+1,last_error=null,next_attempt_at=null where"
              + " event_id=?",
          OffsetDateTime.now(),
          r.get("event_id"));
      log.info("KAFKA_PUBLISHED eventId={} sagaId={}",r.get("event_id"),r.get("aggregate_id"));
    } catch (Exception ex) {
      if (ex instanceof InterruptedException) Thread.currentThread().interrupt();
      int attempts = ((Number) r.get("attempts")).intValue() + 1;
      long delay = Math.min(60, 1L << Math.min(attempts, 6));
      db.update(
          "update event_outbox set attempts=?,last_error=?,next_attempt_at=? where event_id=?",
          attempts,
          ex.getClass().getSimpleName(),
          OffsetDateTime.now().plusSeconds(delay),
          r.get("event_id"));
      log.warn("Evento {} sigue pendiente; intento {}", r.get("event_id"), attempts);
    }
    return !Thread.currentThread().isInterrupted();
  }
}
