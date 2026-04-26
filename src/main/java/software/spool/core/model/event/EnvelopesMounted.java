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
public record EnvelopesMounted(
        String eventId,
        String causationId,
        String correlationId,
        Instant timestamp,
        PartitionKey partitionKey) implements SpoolEvent {
    public EnvelopesMounted {
        Objects.requireNonNull(eventId, "eventId is required");
        Objects.requireNonNull(timestamp, "timestamp is required");
        Objects.requireNonNull(partitionKey, "partitionKey is required");
    }

    public static EnvelopesMounted.Builder builder() {
        return new EnvelopesMounted.Builder();
    }

    public static class Builder {
        private String correlationId;
        private String causationId;
        private PartitionKey partitionKey;

        public EnvelopesMounted.Builder from(final SpoolEvent cause) {
            this.correlationId = cause.correlationId();
            this.causationId = cause.eventId();
            return this;
        }

        public EnvelopesMounted.Builder from(final EnvelopePersisted cause) {
            this.correlationId = cause.correlationId();
            this.causationId = cause.eventId();
            this.partitionKey = cause.partitionKey();
            return this;
        }

        public EnvelopesMounted.Builder correlationId(final String correlationId) {
            this.correlationId = correlationId;
            return this;
        }

        public EnvelopesMounted.Builder causationId(final String causationId) {
            this.causationId = causationId;
            return this;
        }

        public EnvelopesMounted.Builder partitionKey(final PartitionKey partitionKey) {
            this.partitionKey = partitionKey;
            return this;
        }

        public EnvelopesMounted build() {
            return new EnvelopesMounted(
                    UUID.randomUUID().toString(),
                    causationId,
                    correlationId,
                    Instant.now(),
                    partitionKey);
        }
    }
}
