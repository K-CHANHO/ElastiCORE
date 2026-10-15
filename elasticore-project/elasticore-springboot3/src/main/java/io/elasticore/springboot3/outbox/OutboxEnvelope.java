package io.elasticore.springboot3.outbox;

import java.time.Instant;
import java.util.Objects;

/**
 * One recorded domain event as it leaves the service: what happened to which
 * aggregate, when, and the event itself as JSON.
 */
public final class OutboxEnvelope {

    private final Long id;
    private final String aggregateType;
    private final String aggregateId;
    private final String eventType;
    private final String payload;
    private final Instant occurredOn;

    public OutboxEnvelope(Long id, String aggregateType, String aggregateId, String eventType,
                          String payload, Instant occurredOn) {
        this.id = id;
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.payload = payload;
        this.occurredOn = occurredOn;
    }

    /** Stable across delivery attempts: the key a consumer deduplicates by. */
    public Long getId() { return id; }

    public String getAggregateType() { return aggregateType; }

    public String getAggregateId() { return aggregateId; }

    /** The event class's fully qualified name. */
    public String getEventType() { return eventType; }

    /** The event, serialized with the application's ObjectMapper. */
    public String getPayload() { return payload; }

    public Instant getOccurredOn() { return occurredOn; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OutboxEnvelope)) return false;
        return Objects.equals(id, ((OutboxEnvelope) o).id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "OutboxEnvelope{id=" + id + ", " + aggregateType + ":" + aggregateId + ", " + eventType + "}";
    }
}
