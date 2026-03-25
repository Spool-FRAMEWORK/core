package software.spool.core.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Emitted when an inbox item has been published to the event bus by the
 * Publisher.
 *
 * <p>
 * The Ingester module subscribes to this event to begin buffering and
 * persisting the payload to the data lake.
 * </p>
 */
public record ItemPublished(
        String eventId,
        Instant timestamp,
        String correlationId,
        String causationId,
        IdempotencyKey idempotencyKey,
        PartitionKeySchema partitionKeySchema,
        String payload,
        EventMetadata metadata) implements SpoolEvent {

    public ItemPublished {
        Objects.requireNonNull(eventId, "eventId is required");
        Objects.requireNonNull(timestamp, "timestamp is required");
        Objects.requireNonNull(idempotencyKey, "idempotencyKey is required");
        Objects.requireNonNull(partitionKeySchema, "partitionKeySchema is required");
        Objects.requireNonNull(payload, "payload is required");
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String correlationId;
        private String causationId;
        private IdempotencyKey idempotencyKey;
        private PartitionKeySchema partitionKeySchema;
        private String payload;
        private final EventMetadata metadata = new EventMetadata();

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

        public Builder idempotencyKey(final IdempotencyKey idempotencyKey) {
            this.idempotencyKey = idempotencyKey;
            return this;
        }

        public Builder partitionKeySchema(final PartitionKeySchema partitionKeySchema) {
            this.partitionKeySchema = partitionKeySchema;
            return this;
        }

        public Builder payload(final String payload) {
            this.payload = payload;
            return this;
        }

        public Builder addMetadata(EventMetadataKey metadataKey, String value) {
            metadata.set(metadataKey, value);
            return this;
        }

        public Builder addMetadata(EventMetadata metadata) {
            metadata.entrySet().forEach(entry -> metadata.set(entry.getKey(), entry.getValue()));
            return this;
        }

        public ItemPublished build() {
            return new ItemPublished(
                    UUID.randomUUID().toString(),
                    Instant.now(),
                    correlationId,
                    causationId,
                    idempotencyKey,
                    partitionKeySchema,
                    payload,
                    metadata);
        }
    }
}
