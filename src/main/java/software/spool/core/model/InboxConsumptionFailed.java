package software.spool.core.model;

import java.time.Instant;
import java.util.UUID;

public record InboxConsumptionFailed(
        String eventId,
        String eventType,
        Instant timestamp,
        String sender,
        String errorMessage
) implements SpoolEvent {
    public static Builder from(String sender) {
        return new Builder(sender);
    }

    public static class Builder {
        private final String sender;

        protected Builder(String sender) {
            this.sender = sender;
        }

        public InboxConsumptionFailed with(String errorMessage) {
            return new InboxConsumptionFailed(
                    UUID.randomUUID().toString(),
                    "INBOX_FAILED",
                    Instant.now(),
                    sender,
                    errorMessage
            );
        }
    }
}
