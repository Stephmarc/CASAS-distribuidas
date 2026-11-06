package pe.edu.upeu.casaonada.propiedad.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "outbox_events", indexes = {
    @Index(name = "idx_outbox_pendientes", columnList = "published_at, created_at")
})
public class OutboxEvent {
    @Id
    private UUID id;
    @Column(name = "aggregate_type", nullable = false, length = 80)
    private String aggregateType;
    @Column(name = "aggregate_id", nullable = false, length = 80)
    private String aggregateId;
    @Column(name = "event_type", nullable = false, length = 120)
    private String eventType;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    @Column(name = "published_at")
    private Instant publishedAt;

    protected OutboxEvent() { }

    public OutboxEvent(UUID id, String aggregateType, String aggregateId,
                       String eventType, String payload, Instant createdAt) {
        this.id = id;
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.payload = payload;
        this.createdAt = createdAt;
    }
    public UUID getId() { return id; }
    public String getAggregateId() { return aggregateId; }
    public String getEventType() { return eventType; }
    public String getPayload() { return payload; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getPublishedAt() { return publishedAt; }
}
