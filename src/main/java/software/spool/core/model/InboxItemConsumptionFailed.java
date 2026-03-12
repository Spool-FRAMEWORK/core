package software.spool.core.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Emitted when consuming an inbox item fails during ingestion or publishing.
 *
 * <p>
 * Carries the {@link #errorMessage()} describing the root cause.
 * </p>
 */
public record InboxItemConsumptionFailed(
        String eventId,
        Instant timestamp,
        String correlationId,
        String causationId,
        IdempotencyKey idempotencyKey,
        String errorMessage) implements SpoolEvent {

    public InboxItemConsumptionFailed {
        Objects.requireNonNull(eventId, "eventId is required");
        Objects.requireNonNull(timestamp, "timestamp is required");
        Objects.requireNonNull(idempotencyKey, "idempotencyKey is required");
        Objects.requireNonNull(errorMessage, "errorMessage is required");
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String correlationId;
        private String causationId;
        private IdempotencyKey idempotencyKey;
        private String errorMessage;

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

        public Builder errorMessage(final String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public InboxItemConsumptionFailed build() {
            return new InboxItemConsumptionFailed(
                    UUID.randomUUID().toString(),
                    Instant.now(),
                    correlationId,
                    causationId,
                    idempotencyKey,
                    errorMessage);
        }
    }
}