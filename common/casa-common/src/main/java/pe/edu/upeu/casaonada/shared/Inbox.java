package pe.edu.upeu.casaonada.shared;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.OffsetDateTime;
@Component
public class Inbox {
    private final JdbcTemplate db;
    public Inbox(JdbcTemplate db) {this.db=db;}
    @Transactional(propagation=org.springframework.transaction.annotation.Propagation.MANDATORY)
    public boolean first(OperacionEvent event) {
        boolean first = db.update("insert into event_inbox(event_id,processed_at) values (?,?) on conflict (event_id) do nothing",event.eventId(),OffsetDateTime.now())==1;
        org.slf4j.LoggerFactory.getLogger(getClass()).info("KAFKA_CONSUMED type={} eventId={} sagaId={} traceId={} duplicate={}",event.type(),event.eventId(),event.sagaId(),event.traceId(),!first);
        return first;
    }
}
