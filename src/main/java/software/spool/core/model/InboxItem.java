package software.spool.core.model;

import java.time.Instant;

public record InboxItem(
        IdempotencyKey idempotencyKey,
        String payload,
        InboxItemStatus status,
        Instant timestamp
) {
    public InboxItem withStatus(InboxItemStatus newStatus) {
        return new InboxItem(idempotencyKey, payload, newStatus, timestamp);
    }
}
