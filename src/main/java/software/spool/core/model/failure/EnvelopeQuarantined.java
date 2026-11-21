package software.spool.core.model.failure;

import software.spool.core.model.annotation.Delivery;
import software.spool.core.model.SpoolFailedEvent;
import software.spool.core.model.vo.IdempotencyKey;
import software.spool.core.model.SpoolEvent;
import software.spool.core.model.quarantine.QuarantineLocation;
import software.spool.core.model.quarantine.QuarantineReason;
import software.spool.core.port.bus.DeliveryPolicy;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Emitted when an envelope has to go to quarantine, with the reason, the violations when it failed a validation,
 * and where the envelope stayed. For an envelope in the inbox, the janitor moves it to quarantine and keeps its
 * record; for one in bronze, the mounter has already kept it.
 */
@Delivery(DeliveryPolicy.EVERY_SUBSCRIBER)
public record EnvelopeQuarantined(
        String eventId,
        String causationId,
        String correlationId,
        Instant timestamp,
        IdempotencyKey idempotencyKey,
        QuarantineReason reason,
        List<String> violations,
        QuarantineLocation location) implements SpoolFailedEvent {
    public EnvelopeQuarantined {
        Objects.requireNonNull(eventId, "eventId is required");
        Objects.requireNonNull(timestamp, "timestamp is required");
        Objects.requireNonNull(idempotencyKey, "idempotencyKey is required");
        Objects.requireNonNull(reason, "reason is required");
        violations = Objects.isNull(violations) ? List.of() : List.copyOf(violations);
        location = Objects.requireNonNullElseGet(location, QuarantineLocation::inbox);
    }

    public static EnvelopeQuarantined.Builder builder() {
        return new EnvelopeQuarantined.Builder();
    }

    @Override
    public String errorMessage() {
        return "Envelope quarantined: " + reason + " " + violations;
    }

    public static class Builder {
        private String correlationId;
        private String causationId;
        private IdempotencyKey idempotencyKey;
        private QuarantineReason reason;
        private List<String> violations;
        private QuarantineLocation location = QuarantineLocation.inbox();

        public EnvelopeQuarantined.Builder from(final SpoolEvent cause) {
            this.correlationId = cause.correlationId();
            this.causationId = cause.eventId();
            return this;
        }

        public EnvelopeQuarantined.Builder correlationId(final String correlationId) {
            this.correlationId = correlationId;
            return this;
        }

        public EnvelopeQuarantined.Builder causationId(final String causationId) {
            this.causationId = causationId;
            return this;
        }

        public EnvelopeQuarantined.Builder idempotencyKey(final IdempotencyKey idempotencyKey) {
            this.idempotencyKey = idempotencyKey;
            return this;
        }

        public EnvelopeQuarantined.Builder reason(final QuarantineReason reason) {
            this.reason = reason;
            return this;
        }

        public EnvelopeQuarantined.Builder violations(final List<String> violations) {
            this.violations = violations;
            return this;
        }

        public EnvelopeQuarantined.Builder location(final QuarantineLocation location) {
            this.location = location;
            return this;
        }

        public EnvelopeQuarantined build() {
            return new EnvelopeQuarantined(
                    UUID.randomUUID().toString(),
                    causationId,
                    correlationId,
                    Instant.now(),
                    idempotencyKey,
                    reason,
                    violations,
                    location);
        }
    }
}
