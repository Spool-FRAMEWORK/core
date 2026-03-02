package software.spool.core.model;

import java.time.Instant;
import java.util.UUID;

public record ItemPublishFailed(
        String eventId,
        String eventType,
        Instant timestamp,
        String sender,
        String idempotencyKey,
        String errorMessage,
        String payload
) implements SpoolEvent {
    public static ItemPublishFailed from(InboxItemStored origin, String reason) {
        return new ItemPublishFailed(
                UUID.randomUUID().toString(),
                "RAW_DATA_PUBLISH_FAILED",
                Instant.now(),
                origin.sender(),
                origin.idempotencyKey(),
                reason,
                origin.payload()
        );
    }
}
