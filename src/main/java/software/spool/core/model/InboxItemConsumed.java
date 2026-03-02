package software.spool.core.model;

import java.time.Instant;
import java.util.UUID;

public record InboxItemConsumed(
        String eventId,
        Instant timestamp,
        String correlationId,
        String causationId,
        String publisherId,
        String idempotencyKey
) implements SpoolEvent {
    public static class Builder {
        private String correlationId;
        private String causationId;
        private String publisherId;
        private String idempotencyKey;

        public Builder correlationId(String correlationId) {
            this.correlationId = correlationId;
            return this;
        }

        public Builder causationId(String causationId) {
            this.causationId = causationId;
            return this;
        }

        public Builder publisherId(String publisherId) {
            this.publisherId = publisherId;
            return this;
        }

        public Builder idempotencyKey(String idempotencyKey) {
            this.idempotencyKey = idempotencyKey;
            return this;
        }

        public InboxItemConsumed build() {
            return new InboxItemConsumed(
                    UUID.randomUUID().toString(),
                    Instant.now(),
                    correlationId,
                    causationId,
                    publisherId,
                    idempotencyKey
            );
        }
    }
}
