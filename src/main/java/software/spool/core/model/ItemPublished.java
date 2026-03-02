package software.spool.core.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record ItemPublished(
        String eventId,
        Instant timestamp,
        String correlationId,
        String causationId,
        String publisherId,
        String idempotencyKey,
        String destination) implements SpoolEvent {

    public ItemPublished {
        Objects.requireNonNull(eventId, "eventId is required");
        Objects.requireNonNull(timestamp, "timestamp is required");
        Objects.requireNonNull(publisherId, "publisherId is required");
        Objects.requireNonNull(idempotencyKey, "idempotencyKey is required");
        Objects.requireNonNull(destination, "destination is required");
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String correlationId;
        private String causationId;
        private String publisherId;
        private String idempotencyKey;
        private String destination;

        public Builder from(final SpoolEvent cause) {
            this.correlationId = cause.correlationId();
            this.causationId = cause.eventId();
            return this;
        }

        public Builder from(final InboxItemStored cause) {
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

        public Builder publisherId(final String publisherId) {
            this.publisherId = publisherId;
            return this;
        }

        public Builder idempotencyKey(final String idempotencyKey) {
            this.idempotencyKey = idempotencyKey;
            return this;
        }

        public Builder destination(final String destination) {
            this.destination = destination;
            return this;
        }

        public ItemPublished build() {
            return new ItemPublished(
                    UUID.randomUUID().toString(),
                    Instant.now(),
                    correlationId,
                    causationId,
                    publisherId,
                    idempotencyKey,
                    destination);
        }
    }
}
