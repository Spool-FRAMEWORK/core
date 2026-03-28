package software.spool.core.model.failure;

import software.spool.core.model.vo.IdempotencyKey;
import software.spool.core.model.SpoolEvent;
import software.spool.core.model.event.ItemPublished;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Emitted when an item has been successfully persisted to the data lake by the
 * Ingester.
 */
public record ItemQuarantined(
        String eventId,
        String causationId,
        String correlationId,
        Instant timestamp,
        IdempotencyKey idempotencyKey,
        List<String> violations) implements SpoolEvent {
    public ItemQuarantined {
        Objects.requireNonNull(eventId, "eventId is required");
        Objects.requireNonNull(timestamp, "timestamp is required");
        Objects.requireNonNull(idempotencyKey, "idempotencyKey is required");
    }

    public static ItemQuarantined.Builder builder() {
        return new ItemQuarantined.Builder();
    }

    public static class Builder {
        private String correlationId;
        private String causationId;
        private IdempotencyKey idempotencyKey;
        private List<String> violations;

        public ItemQuarantined.Builder from(final SpoolEvent cause) {
            this.correlationId = cause.correlationId();
            this.causationId = cause.eventId();
            return this;
        }

        public ItemQuarantined.Builder from(final ItemPublished cause) {
            this.correlationId = cause.correlationId();
            this.causationId = cause.eventId();
            this.idempotencyKey = cause.idempotencyKey();
            return this;
        }

        public ItemQuarantined.Builder correlationId(final String correlationId) {
            this.correlationId = correlationId;
            return this;
        }

        public ItemQuarantined.Builder causationId(final String causationId) {
            this.causationId = causationId;
            return this;
        }

        public ItemQuarantined.Builder idempotencyKey(final IdempotencyKey idempotencyKey) {
            this.idempotencyKey = idempotencyKey;
            return this;
        }

        public ItemQuarantined.Builder violations(final List<String> violations) {
            this.violations = violations;
            return this;
        }

        public ItemQuarantined build() {
            return new ItemQuarantined(
                    UUID.randomUUID().toString(),
                    causationId,
                    correlationId,
                    Instant.now(),
                    idempotencyKey,
                    violations);
        }
    }
}
