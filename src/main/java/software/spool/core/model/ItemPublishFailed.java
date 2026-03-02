package software.spool.core.model;

import java.time.Instant;
import java.util.UUID;

public record ItemPublishFailed(
        String eventId,
        Instant timestamp,
        String correlationId,
        String causationId,
        String publisherId,
        String idempotencyKey,
        String destination,
        String errorMessage
) implements SpoolEvent {
    public static class Builder {
        private String correlationId;
        private String causationId;
        private String publisherId;
        private String idempotencyKey;
        private String destination;
        private String errorMessage;

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

        public Builder destination(String destination) {
            this.destination = destination;
            return this;
        }

        public Builder errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public ItemPublishFailed build() {
            return new ItemPublishFailed(
                    UUID.randomUUID().toString(),
                    Instant.now(),
                    correlationId,
                    causationId,
                    publisherId,
                    idempotencyKey,
                    destination,
                    errorMessage
            );
        }
    }
}
