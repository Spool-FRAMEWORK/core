package software.spool.core.model.event;

import software.spool.core.model.SpoolEvent;
import software.spool.core.model.annotation.Delivery;
import software.spool.core.model.vo.IdempotencyKey;
import software.spool.core.port.bus.DeliveryPolicy;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Emitted when an envelope leaves quarantine and goes back to the inbox to be processed again.
 *
 * <p>
 * {@code republishedIdempotencyKey} is the key the envelope has in the inbox now: the same as
 * {@code idempotencyKey} when it was republished as it was, or a new one when its payload was corrected.
 * </p>
 */
@Delivery(DeliveryPolicy.EVERY_SUBSCRIBER)
public record EnvelopeRepublished(
        String eventId,
        Instant timestamp,
        String correlationId,
        String causationId,
        IdempotencyKey idempotencyKey,
        IdempotencyKey republishedIdempotencyKey) implements SpoolEvent {

    public EnvelopeRepublished {
        Objects.requireNonNull(eventId, "eventId is required");
        Objects.requireNonNull(timestamp, "timestamp is required");
        Objects.requireNonNull(idempotencyKey, "idempotencyKey is required");
        Objects.requireNonNull(republishedIdempotencyKey, "republishedIdempotencyKey is required");
    }

    public boolean wasCorrected() {
        return !idempotencyKey.equals(republishedIdempotencyKey);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String correlationId;
        private String causationId;
        private IdempotencyKey idempotencyKey;
        private IdempotencyKey republishedIdempotencyKey;

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

        public EnvelopeRepublished build() {
            return new EnvelopeRepublished(
                    UUID.randomUUID().toString(),
                    Instant.now(),
                    correlationId,
                    causationId,
                    idempotencyKey,
                    republishedIdempotencyKey);
        }
    }
}
