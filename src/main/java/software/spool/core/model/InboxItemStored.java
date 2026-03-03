package software.spool.core.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record InboxItemStored(
        String eventId,
        Instant timestamp,
        String correlationId,
        String causationId,
        String senderId,
        String sourceId,
        String idempotencyKey) implements SpoolEvent {

    public InboxItemStored {
        Objects.requireNonNull(eventId, "eventId is required");
        Objects.requireNonNull(timestamp, "timestamp is required");
        Objects.requireNonNull(senderId, "senderId is required");
        Objects.requireNonNull(sourceId, "sourceId is required");
        Objects.requireNonNull(idempotencyKey, "idempotencyKey is required");
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String correlationId;
        private String causationId;
        private String senderId;
        private String sourceId;
        private String idempotencyKey;

        public Builder from(final SpoolEvent cause) {
            this.correlationId = cause.correlationId();
            this.causationId = cause.eventId();
            return this;
        }

        public Builder from(final SourceItemCaptured cause) {
            this.correlationId = cause.correlationId();
            this.causationId = cause.eventId();
            this.senderId = cause.senderId();
            this.sourceId = cause.sourceId();
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

        public Builder crawlerId(final String crawlerId) {
            this.senderId = crawlerId;
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

        public InboxItemStored build() {
            return new InboxItemStored(
                    UUID.randomUUID().toString(),
                    Instant.now(),
                    correlationId,
                    causationId,
                    senderId,
                    sourceId,
                    idempotencyKey);
        }
    }
}
