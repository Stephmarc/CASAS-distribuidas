package pe.edu.upeu.casaonada.pago.messaging;

import org.slf4j.*;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.casaonada.pago.domain.EstadoPago;
import pe.edu.upeu.casaonada.pago.repository.PagoRepository;
import pe.edu.upeu.casaonada.pago.service.PagoService;
import pe.edu.upeu.casaonada.shared.*;
import tools.jackson.databind.ObjectMapper;

@Component
public class PagoEvents {
  private final pe.edu.upeu.casaonada.pago.service.PagoCancellationFence fence;
  private final ObjectMapper json;
  private final Inbox inbox;
  private final PagoService service;
  private final PagoRepository repo;
  private static final Logger log = LoggerFactory.getLogger(PagoEvents.class);

  public PagoEvents(
      ObjectMapper j,
      Inbox i,
      PagoService s,
      PagoRepository r,
      pe.edu.upeu.casaonada.pago.service.PagoCancellationFence f) {
    fence = f;
    json = j;
    inbox = i;
    service = s;
    repo = r;
  }

  @KafkaListener(topics = Outbox.TOPIC)
  @Transactional
  public void accept(String payload) {
    var e = json.readValue(payload, OperacionEvent.class);
    e.validate();
    if (!e.type().equals("REEMBOLSAR_PAGO") || !inbox.first(e)) return;
    fence.close(e);
    service.compensar(e);
  }

  @Scheduled(fixedDelay = 30000)
  public void poll() {
    for (var p : repo.findByEstadoIn(java.util.List.of(EstadoPago.CREADO, EstadoPago.PENDIENTE))) {
      if (!p.getProveedor().equals("MERCADO_PAGO")) continue;
      try {
        service.reconciliar(p.getId());
      } catch (Exception ex) {
        log.warn("Pago {} pendiente de conciliacion: {}", p.getId(), ex.getClass().getSimpleName());
      }
    }
  }
}
