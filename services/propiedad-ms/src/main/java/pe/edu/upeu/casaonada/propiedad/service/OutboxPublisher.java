package pe.edu.upeu.casaonada.propiedad.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pe.edu.upeu.casaonada.propiedad.entity.OutboxEvent;
import pe.edu.upeu.casaonada.propiedad.repository.OutboxEventRepository;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@ConditionalOnProperty(name = "app.events.enabled", havingValue = "true")
public class OutboxPublisher {
    private static final Logger log = LoggerFactory.getLogger(OutboxPublisher.class);
    private final OutboxEventRepository repository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final String topic;

    public OutboxPublisher(OutboxEventRepository repository,
            KafkaTemplate<String, String> kafkaTemplate,
            org.springframework.core.env.Environment environment) {
        this.repository = repository;
        this.kafkaTemplate = kafkaTemplate;
        this.topic = environment.getProperty("app.events.topic", "propiedad-eventos");
    }

    @Scheduled(fixedDelayString = "${app.events.publisher-delay-ms:1500}")
    public void publicarPendientes() {
        List<OutboxEvent> pending = repository.findTop50ByPublishedAtIsNullOrderByCreatedAtAsc();
        for (OutboxEvent event : pending) {
            try {
                kafkaTemplate.send(topic, event.getAggregateId(), event.getPayload())
                        .get(5, TimeUnit.SECONDS);
                repository.marcarPublicado(event.getId(), Instant.now());
                log.info("Evento publicado: eventId={}, type={}, aggregateId={}",
                        event.getId(), event.getEventType(), event.getAggregateId());
            } catch (Exception ex) {
                log.warn("No se pudo publicar eventId={}. Se reintentará: {}",
                        event.getId(), ex.getMessage());
                break;
            }
        }
    }
}
