package pe.edu.upeu.casaonada.orden.messaging;

import java.time.*;
import java.util.*;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.casaonada.orden.domain.*;
import pe.edu.upeu.casaonada.orden.repository.OrdenRepository;
import pe.edu.upeu.casaonada.orden.service.OrdenService;
import pe.edu.upeu.casaonada.shared.*;
import tools.jackson.databind.ObjectMapper;

@Component
public class OrdenEvents {
  private final jakarta.persistence.EntityManager em;
  private final Inbox inbox;
  private final Outbox outbox;
  private final ObjectMapper json;
  private final OrdenRepository repo;
  private final OrdenService service;

  public OrdenEvents(
      Inbox i,
      Outbox o,
      ObjectMapper j,
      OrdenRepository r,
      OrdenService s,
      jakarta.persistence.EntityManager e) {
    em = e;
    inbox = i;
    outbox = o;
    json = j;
    repo = r;
    service = s;
  }

  @KafkaListener(topics = Outbox.TOPIC)
  @Transactional
  public void accept(String payload) {
    var e = json.readValue(payload, OperacionEvent.class);
    e.validate();
    if (!Set.of(
            "RESERVA_ASOCIADA",
            "RESERVA_RECHAZADA",
            "PROPIEDAD_BLOQUEADA",
            "PROPIEDAD_RECHAZADA",
            "PAGO_APROBADO",
            "PAGO_RECHAZADO",
            "CONTRATO_GENERADO",
            "CONTRATO_RECHAZADO",
            "PROPIEDAD_CONFIRMADA",
            "RESERVA_CONVERTIDA",
            "CONTRATO_CANCELADO",
            "PAGO_REEMBOLSADO",
            "PROPIEDAD_LIBERADA",
            "RESERVA_CANCELADA")
        .contains(e.type())) return;
    if (!inbox.first(e)) return;
    var o = repo.locked(e.ordenId()).orElseThrow();
    if (!service.event(o, e.type()).sameOperation(e))
      throw new IllegalArgumentException("Evento incompatible con orden");
    if (!o.estado.terminal()
        && !o.estado.compensando()
        && o.deadline.isBefore(OffsetDateTime.now()))
      service.advance(o, "TIMEOUT", "Plazo de reserva o de etapa agotado");
    else service.advance(o, e.type(), e.motivo());
  }

  @Scheduled(fixedDelay = 30000)
  @Transactional
  public void recover() {
    for (var candidate :
        repo.findTop100ByDeadlineBeforeAndEstadoNotIn(
            OffsetDateTime.now(), List.of(EstadoOrden.CONFIRMADA, EstadoOrden.CANCELADA))) {
      var o = repo.locked(candidate.id).orElseThrow();
      em.refresh(o, jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
      if (o.estado.terminal() || o.deadline.isAfter(OffsetDateTime.now())) continue;
      if (o.estado.compensando()) {
        outbox.add(service.event(o, SagaTransitions.command(o.estado)));
        o.deadline = OffsetDateTime.now().plusMinutes(2);
        repo.save(o);
      } else service.advance(o, "TIMEOUT", "Tiempo de la etapa agotado");
    }
  }
}
