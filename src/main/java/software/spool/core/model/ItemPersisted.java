package software.spool.core.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record ItemPersisted(
        String eventId,
        String causationId,
        String correlationId,
        Instant timestamp,
        IdempotencyKey idempotencyKey
) implements SpoolEvent{
    public ItemPersisted {
        Objects.requireNonNull(eventId, "eventId is required");
        Objects.requireNonNull(timestamp, "timestamp is required");
        Objects.requireNonNull(idempotencyKey, "idempotencyKey is required");
    }

    public static ItemPersisted.Builder builder() {
        return new ItemPersisted.Builder();
    }

    public static class Builder {
        private String correlationId;
        private String causationId;
        private IdempotencyKey idempotencyKey;

        public ItemPersisted.Builder from(final SpoolEvent cause) {
            this.correlationId = cause.correlationId();
            this.causationId = cause.eventId();
            return this;
        }

        public ItemPersisted.Builder from(final ItemPublished cause) {
            this.correlationId = cause.correlationId();
            this.causationId = cause.eventId();
            this.idempotencyKey = cause.idempotencyKey();
            return this;
        }

        public ItemPersisted.Builder correlationId(final String correlationId) {
            this.correlationId = correlationId;
            return this;
        }

        public ItemPersisted.Builder causationId(final String causationId) {
            this.causationId = causationId;
            return this;
        }

        public ItemPersisted.Builder idempotencyKey(final IdempotencyKey idempotencyKey) {
            this.idempotencyKey = idempotencyKey;
            return this;
        }

        public ItemPersisted build() {
            return new ItemPersisted(
                    UUID.randomUUID().toString(),
                    causationId,
                    correlationId,
                    Instant.now(),
                    idempotencyKey);
        }
    }
}
