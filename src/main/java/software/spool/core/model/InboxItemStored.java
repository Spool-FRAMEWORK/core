package software.spool.core.model;

import java.time.Instant;
import java.util.UUID;

public record InboxItemStored(
        String eventId,
        Instant timestamp,
        String eventType,
        String sender,
        String idempotencyKey
) implements SpoolEvent {

    public static Builder from(String source) {
        return new Builder(source);
    }

    public static class Builder {
        private final String sender;
        private String idempotencyKey;

        public Builder(String sender) {
            this.sender = sender;
        }

        public Builder withIdempotencyKey(String idempotencyKey) {
            this.idempotencyKey = idempotencyKey;
            return this;
        }

        public InboxItemStored create() {
            return new InboxItemStored(
                    UUID.randomUUID().toString(),
                    Instant.now(),
                    "DataWrittenToInbox",
                    sender,
                    idempotencyKey
            );
        }
    }
}
