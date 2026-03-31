package software.spool.core.model.event;

import software.spool.core.model.vo.PartitionKey;
import software.spool.core.model.SpoolEvent;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Emitted when an item has been successfully persisted to the data lake by the
 * Ingester.
 */
public record ItemsMounted(
        String eventId,
        String causationId,
        String correlationId,
        Instant timestamp,
        PartitionKey partitionKey) implements SpoolEvent {
    public ItemsMounted {
        Objects.requireNonNull(eventId, "eventId is required");
        Objects.requireNonNull(timestamp, "timestamp is required");
        Objects.requireNonNull(partitionKey, "partitionKey is required");
    }

    public static ItemsMounted.Builder builder() {
        return new ItemsMounted.Builder();
    }

    public static class Builder {
        private String correlationId;
        private String causationId;
        private PartitionKey partitionKey;

        public ItemsMounted.Builder from(final SpoolEvent cause) {
            this.correlationId = cause.correlationId();
            this.causationId = cause.eventId();
            return this;
        }

        public ItemsMounted.Builder from(final ItemPersisted cause) {
            this.correlationId = cause.correlationId();
            this.causationId = cause.eventId();
            this.partitionKey = cause.partitionKey();
            return this;
        }

        public ItemsMounted.Builder correlationId(final String correlationId) {
            this.correlationId = correlationId;
            return this;
        }

        public ItemsMounted.Builder causationId(final String causationId) {
            this.causationId = causationId;
            return this;
        }

        public ItemsMounted.Builder partitionKey(final PartitionKey partitionKey) {
            this.partitionKey = partitionKey;
            return this;
        }

        public ItemsMounted build() {
            return new ItemsMounted(
                    UUID.randomUUID().toString(),
                    causationId,
                    correlationId,
                    Instant.now(),
                    partitionKey);
        }
    }
}
