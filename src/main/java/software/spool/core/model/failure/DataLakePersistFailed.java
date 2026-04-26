package software.spool.core.model.failure;

import software.spool.core.model.SpoolEvent;
import software.spool.core.model.event.SourcePayloadCaptured;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Emitted when the Ingester fails to persist a batch of items to the data lake.
 *
 * <p>
 * Carries the {@link #errorMessage()} describing the root cause.
 * </p>
 */
public record DataLakePersistFailed(
        String eventId,
        Instant timestamp,
        String correlationId,
        String causationId,
        String errorMessage) implements SpoolEvent {

    public DataLakePersistFailed {
        Objects.requireNonNull(eventId, "eventId is required");
        Objects.requireNonNull(timestamp, "timestamp is required");
        Objects.requireNonNull(errorMessage, "errorMessage is required");
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String correlationId;
        private String causationId;
        private String errorMessage;

        public Builder from(final SpoolEvent cause) {
            this.correlationId = cause.correlationId();
            this.causationId = cause.eventId();
            return this;
        }

        public Builder from(final SourcePayloadCaptured cause) {
            this.correlationId = cause.correlationId();
            this.causationId = cause.eventId();
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

        public Builder errorMessage(final String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public DataLakePersistFailed build() {
            return new DataLakePersistFailed(
                    UUID.randomUUID().toString(),
                    Instant.now(),
                    correlationId,
                    causationId,
                    errorMessage);
        }
    }
}
