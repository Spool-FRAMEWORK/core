package software.spool.core.model.event;

import software.spool.core.model.vo.IdempotencyKey;
import software.spool.core.model.SpoolEvent;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Emitted when an inbox item has been successfully consumed by the Ingester.
 */
public record EnvelopeConsumed(
        String eventId,
        Instant timestamp,
        String correlationId,
        String causationId,
        IdempotencyKey idempotencyKey) implements SpoolEvent {

    public EnvelopeConsumed {
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

        public Builder from(final SpoolEvent cause) {
            this.correlationId = cause.correlationId();
            this.causationId = cause.eventId();
            return this;
        }

        public Builder from(final ItemPublished cause) {
            this.correlationId = cause.correlationId();
            this.causationId = cause.eventId();
            this.idempotencyKey = cause.idempotencyKey();
            return this;
        }

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

        public EnvelopeConsumed build() {
            return new EnvelopeConsumed(
                    UUID.randomUUID().toString(),
                    Instant.now(),
                    correlationId,
                    causationId,
                    idempotencyKey);
        }
    }
}
