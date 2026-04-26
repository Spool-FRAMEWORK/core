package software.spool.core.model.event;

import software.spool.core.model.vo.IdempotencyKey;
import software.spool.core.model.vo.PartitionKey;
import software.spool.core.model.SpoolEvent;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Emitted when an item has been successfully persisted to the data lake by the
 * Ingester.
 */
public record EnvelopePersisted(
        String eventId,
        String causationId,
        String correlationId,
        Instant timestamp,
        IdempotencyKey idempotencyKey,
        PartitionKey partitionKey) implements SpoolEvent {
    public EnvelopePersisted {
        Objects.requireNonNull(eventId, "eventId is required");
        Objects.requireNonNull(timestamp, "timestamp is required");
        Objects.requireNonNull(idempotencyKey, "idempotencyKey is required");
        Objects.requireNonNull(partitionKey, "partitionKey is required");
    }

    public static EnvelopePersisted.Builder builder() {
        return new EnvelopePersisted.Builder();
    }

    public static class Builder {
        private String correlationId;
        private String causationId;
        private IdempotencyKey idempotencyKey;
        private PartitionKey partitionKey;

        public EnvelopePersisted.Builder from(final SpoolEvent cause) {
            this.correlationId = cause.correlationId();
            this.causationId = cause.eventId();
            return this;
        }

        public EnvelopePersisted.Builder from(final ItemPublished cause) {
            this.correlationId = cause.correlationId();
            this.causationId = cause.eventId();
            this.idempotencyKey = cause.idempotencyKey();
            return this;
        }

        public EnvelopePersisted.Builder correlationId(final String correlationId) {
            this.correlationId = correlationId;
            return this;
        }

        public EnvelopePersisted.Builder causationId(final String causationId) {
            this.causationId = causationId;
            return this;
        }

        public EnvelopePersisted.Builder idempotencyKey(final IdempotencyKey idempotencyKey) {
            this.idempotencyKey = idempotencyKey;
            return this;
        }

        public EnvelopePersisted.Builder partitionKey(final PartitionKey partitionKey) {
            this.partitionKey = partitionKey;
            return this;
        }

        public EnvelopePersisted build() {
            return new EnvelopePersisted(
                    UUID.randomUUID().toString(),
                    causationId,
                    correlationId,
                    Instant.now(),
                    idempotencyKey,
                    partitionKey);
        }
    }
}
