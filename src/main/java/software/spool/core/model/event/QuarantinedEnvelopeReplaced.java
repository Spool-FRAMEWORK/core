package software.spool.core.model.event;

import software.spool.core.model.SpoolEvent;
import software.spool.core.model.annotation.Delivery;
import software.spool.core.model.vo.IdempotencyKey;
import software.spool.core.port.bus.DeliveryPolicy;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Emitted when a new capture takes the place of an envelope that was in quarantine with the same key, because its
 * source replaces what is in quarantine. The janitor resolves the quarantine record of that key as replaced.
 */
@Delivery(DeliveryPolicy.EVERY_SUBSCRIBER)
public record QuarantinedEnvelopeReplaced(
        String eventId,
        Instant timestamp,
        String correlationId,
        String causationId,
        IdempotencyKey idempotencyKey) implements SpoolEvent {

    public QuarantinedEnvelopeReplaced {
        Objects.requireNonNull(eventId, "eventId is required");
        Objects.requireNonNull(timestamp, "timestamp is required");
        Objects.requireNonNull(idempotencyKey, "idempotencyKey is required");
    }

    public static QuarantinedEnvelopeReplaced of(IdempotencyKey idempotencyKey, String correlationId) {
        return new QuarantinedEnvelopeReplaced(UUID.randomUUID().toString(), Instant.now(), correlationId, null,
                idempotencyKey);
    }
}
