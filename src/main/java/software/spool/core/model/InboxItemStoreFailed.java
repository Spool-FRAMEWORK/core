package software.spool.core.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record InboxItemStoreFailed(
        String eventId,
        Instant timestamp,
        String correlationId,
        String causationId,
        String crawlerId,
        String sourceId,
        String idempotencyKey,
        String errorMessage) implements SpoolEvent {

    public InboxItemStoreFailed {
        Objects.requireNonNull(eventId, "eventId is required");
        Objects.requireNonNull(timestamp, "timestamp is required");
        Objects.requireNonNull(crawlerId, "crawlerId is required");
        Objects.requireNonNull(sourceId, "sourceId is required");
        Objects.requireNonNull(idempotencyKey, "idempotencyKey is required");
        Objects.requireNonNull(errorMessage, "errorMessage is required");
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String correlationId;
        private String causationId;
        private String crawlerId;
        private String sourceId;
        private String idempotencyKey;
        private String errorMessage;

        public Builder from(final SpoolEvent cause) {
            this.correlationId = cause.correlationId();
            this.causationId = cause.eventId();
            return this;
        }

        public Builder from(final SourceItemCaptured cause) {
            this.correlationId = cause.correlationId();
            this.causationId = cause.eventId();
            this.crawlerId = cause.crawlerId();
            this.sourceId = cause.sourceId();
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

        public Builder crawlerId(final String crawlerId) {
            this.crawlerId = crawlerId;
            return this;
        }

        public Builder sourceId(final String sourceId) {
            this.sourceId = sourceId;
            return this;
        }

        public Builder idempotencyKey(final String idempotencyKey) {
            this.idempotencyKey = idempotencyKey;
            return this;
        }

        public Builder errorMessage(final String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public InboxItemStoreFailed build() {
            return new InboxItemStoreFailed(
                    UUID.randomUUID().toString(),
                    Instant.now(),
                    correlationId,
                    causationId,
                    crawlerId,
                    sourceId,
                    idempotencyKey,
                    errorMessage);
        }
    }
}
