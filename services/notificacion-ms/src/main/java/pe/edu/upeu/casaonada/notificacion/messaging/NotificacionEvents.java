package pe.edu.upeu.casaonada.notificacion.messaging;

import java.time.OffsetDateTime;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.casaonada.notificacion.domain.Notificacion;
import pe.edu.upeu.casaonada.notificacion.repository.NotificacionRepository;
import pe.edu.upeu.casaonada.shared.*;
import tools.jackson.databind.ObjectMapper;

@Component
public class NotificacionEvents {
  private final ObjectMapper json;
  private final Inbox inbox;
  private final NotificacionRepository repo;

  public NotificacionEvents(ObjectMapper j, Inbox i, NotificacionRepository r) {
    json = j;
    inbox = i;
    repo = r;
  }

  @KafkaListener(topics = Outbox.TOPIC)
  @Transactional
  public void accept(String payload) {
    var e = json.readValue(payload, OperacionEvent.class);
    e.validate();
    if (!e.type().equals("ORDEN_CONFIRMADA") && !e.type().equals("ORDEN_CANCELADA")) return;
    if (!inbox.first(e)) return;
    var n = new Notificacion();
    n.eventId = e.eventId();
    n.clienteId = e.clienteId();
    n.ordenId = e.ordenId();
    n.tipo = e.type();
    n.createdAt = OffsetDateTime.now();
    n.mensaje =
        e.type().equals("ORDEN_CONFIRMADA")
            ? "Operacion inmobiliaria confirmada. Contrato generado."
            : "Operacion cancelada y compensaciones completadas.";
    repo.save(n);
  }
}
