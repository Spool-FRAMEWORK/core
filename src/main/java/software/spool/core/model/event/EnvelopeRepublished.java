package software.spool.core.model.event;

import software.spool.core.model.SpoolEvent;
import software.spool.core.model.annotation.Delivery;
import software.spool.core.model.vo.IdempotencyKey;
import software.spool.core.port.bus.DeliveryPolicy;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Emitted when an envelope leaves quarantine and goes back to where it stayed, to be processed again.
 *
 * <p>
 * {@code republishedIdempotencyKey} is the key the envelope has now: the same as {@code idempotencyKey} when it was
 * republished as it was or with a bypass, or a new one when its payload was corrected. {@code resolution} says which
 * of the three it was; an event without it, published before it existed, is deduced from the two keys.
 * </p>
 */
@Delivery(DeliveryPolicy.EVERY_SUBSCRIBER)
public record EnvelopeRepublished(
        String eventId,
        Instant timestamp,
        String correlationId,
        String causationId,
        IdempotencyKey idempotencyKey,
        IdempotencyKey republishedIdempotencyKey,
        Resolution resolution) implements SpoolEvent {

    /**
     * How the envelope left quarantine.
     */
    public enum Resolution {
        UNCHANGED,
        CORRECTED,
        BYPASSED
    }

    public EnvelopeRepublished {
        Objects.requireNonNull(eventId, "eventId is required");
        Objects.requireNonNull(timestamp, "timestamp is required");
        Objects.requireNonNull(idempotencyKey, "idempotencyKey is required");
        Objects.requireNonNull(republishedIdempotencyKey, "republishedIdempotencyKey is required");
        if (resolution == null) {
            resolution = idempotencyKey.equals(republishedIdempotencyKey) ? Resolution.UNCHANGED : Resolution.CORRECTED;
        }
    }

    public boolean wasCorrected() {
        return resolution == Resolution.CORRECTED;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String correlationId;
        private String causationId;
        private IdempotencyKey idempotencyKey;
        private IdempotencyKey republishedIdempotencyKey;
        private Resolution resolution;

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

        public Builder republishedIdempotencyKey(final IdempotencyKey republishedIdempotencyKey) {
            this.republishedIdempotencyKey = republishedIdempotencyKey;
            return this;
        }

        public Builder resolution(final Resolution resolution) {
            this.resolution = resolution;
            return this;
        }

        public EnvelopeRepublished build() {
            return new EnvelopeRepublished(
                    UUID.randomUUID().toString(),
                    Instant.now(),
                    correlationId,
                    causationId,
                    idempotencyKey,
                    republishedIdempotencyKey,
                    resolution);
        }
    }
}
