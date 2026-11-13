package software.spool.core.model.event;

import software.spool.core.model.SpoolEvent;
import software.spool.core.model.annotation.Delivery;
import software.spool.core.model.vo.IdempotencyKey;
import software.spool.core.port.bus.DeliveryPolicy;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Emitted when an envelope in quarantine is deleted for good, together with its quarantine record.
 */
@Delivery(DeliveryPolicy.EVERY_SUBSCRIBER)
public record EnvelopePurged(
        String eventId,
        Instant timestamp,
        String correlationId,
        String causationId,
        IdempotencyKey idempotencyKey) implements SpoolEvent {

    public EnvelopePurged {
        Objects.requireNonNull(eventId, "eventId is required");
        Objects.requireNonNull(timestamp, "timestamp is required");
        Objects.requireNonNull(idempotencyKey, "idempotencyKey is required");
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String correlationId;
        private String causationId;
        private IdempotencyKey idempotencyKey;

        public Builder correlationId(final String correlationId) {
            this.correlationId = correlationId;
            return this;
        }

        public Builder causationId(final String causationId) {
            this.causationId = causationId;
            return this;
        }

        public Builder idempotencyKey(final IdempotencyKey idempotencyKey) {
            this.idempotencyKey = idempotencyKey;
            return this;
        }

        public EnvelopePurged build() {
            return new EnvelopePurged(
                    UUID.randomUUID().toString(),
                    Instant.now(),
                    correlationId,
                    causationId,
                    idempotencyKey);
        }
    }
}
