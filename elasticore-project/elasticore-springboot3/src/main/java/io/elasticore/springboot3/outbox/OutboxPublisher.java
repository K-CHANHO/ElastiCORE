package io.elasticore.springboot3.outbox;

/**
 * Where a recorded domain event goes once its transaction has committed:
 * a message broker, a webhook, another service.
 *
 * The generated relay hands each outbox message to the one bean of this type,
 * oldest first, and marks it published when this returns normally. Throwing
 * leaves it unpublished for the next run - delivery is at least once, so a
 * consumer should be ready to see a message twice (the envelope's id is
 * stable across attempts). With no bean of this type the relay does nothing
 * and messages wait in the table.
 */
@FunctionalInterface
public interface OutboxPublisher {

    void publish(OutboxEnvelope message) throws Exception;
}
