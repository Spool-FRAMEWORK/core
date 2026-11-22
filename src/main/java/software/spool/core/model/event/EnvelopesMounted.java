package software.spool.core.model.event;

import software.spool.core.model.annotation.Delivery;
import software.spool.core.model.vo.PartitionKey;
import software.spool.core.model.SpoolEvent;
import software.spool.core.port.bus.DeliveryPolicy;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Emitted when a mount has written results to a partition of a data mart, once per partition and mount, so the
 * mounters that read that data mart can mount it.
 *
 * @param dataMart     the data mart written, named with its layer ({@code silver/payments})
 * @param partitionKey the partition of the data mart that received results
 */
@Delivery(DeliveryPolicy.EVERY_SUBSCRIBER)
public record EnvelopesMounted(
        String eventId,
        String causationId,
        String correlationId,
        Instant timestamp,
        String dataMart,
        PartitionKey partitionKey) implements SpoolEvent {
    public EnvelopesMounted {
        Objects.requireNonNull(eventId, "eventId is required");
        Objects.requireNonNull(timestamp, "timestamp is required");
        Objects.requireNonNull(dataMart, "dataMart is required");
        Objects.requireNonNull(partitionKey, "partitionKey is required");
    }

    public static EnvelopesMounted.Builder builder() {
        return new EnvelopesMounted.Builder();
    }

    public static class Builder {
        private String correlationId;
        private String causationId;
        private String dataMart;
        private PartitionKey partitionKey;

        public EnvelopesMounted.Builder from(final SpoolEvent cause) {
            this.correlationId = cause.correlationId();
            this.causationId = cause.eventId();
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

        public EnvelopesMounted.Builder dataMart(final String dataMart) {
            this.dataMart = dataMart;
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
                    dataMart,
                    partitionKey);
        }
    }
}
