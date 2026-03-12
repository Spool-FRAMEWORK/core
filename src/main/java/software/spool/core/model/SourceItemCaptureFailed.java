package software.spool.core.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Emitted when the Crawler fails to capture or serialize a single record
 * from the source.
 *
 * <p>
 * Carries the {@link #errorMessage()} describing the root cause.
 * </p>
 */
public record SourceItemCaptureFailed(
        String eventId,
        Instant timestamp,
        String correlationId,
        String causationId,
        String errorMessage) implements SpoolEvent {

    public SourceItemCaptureFailed {
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

        public SourceItemCaptureFailed build() {
            return new SourceItemCaptureFailed(
                    UUID.randomUUID().toString(),
                    Instant.now(),
                    correlationId,
                    causationId,
                    errorMessage);
        }
    }
}
