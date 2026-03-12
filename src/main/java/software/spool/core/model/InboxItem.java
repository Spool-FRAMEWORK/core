package software.spool.core.model;

import java.time.Instant;

/**
 * Immutable representation of a single item held in the inbox.
 *
 * <p>
 * An {@code InboxItem} is created when a crawler stores a captured record,
 * and its {@link #status()} is updated as it progresses through publishing
 * and ingestion.
 * </p>
 *
 * @param idempotencyKey unique key used for deduplication
 * @param payload        the serialized record content
 * @param status         current lifecycle status
 * @param timestamp      the instant the item was first stored
 */
public record InboxItem(
        IdempotencyKey idempotencyKey,
        String payload,
        InboxItemStatus status,
        Instant timestamp) {
    public InboxItem withStatus(InboxItemStatus newStatus) {
        return new InboxItem(idempotencyKey, payload, newStatus, timestamp);
    }
}
