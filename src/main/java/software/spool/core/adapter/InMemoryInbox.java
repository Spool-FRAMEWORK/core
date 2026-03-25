package software.spool.core.adapter;

import software.spool.core.model.*;
import software.spool.core.port.InboxReader;
import software.spool.core.port.InboxUpdater;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * In-memory inbox implementation intended for local testing and development.
 *
 * <p>
 * Stores inbox items in a {@link ConcurrentHashMap} keyed by their
 * {@link IdempotencyKey}. This class is used as the default inbox by the
 * publisher and ingester builders when no custom implementation is provided.
 * </p>
 */
public class InMemoryInbox implements InboxUpdater, InboxReader {
    private final ConcurrentHashMap<IdempotencyKey, InboxItem> items = new ConcurrentHashMap<>();

    @Override
    public InboxItem update(IdempotencyKey idempotencyKey, InboxItemStatus status) {
        InboxItem existing = items.get(idempotencyKey);
        if (existing == null)
            return null;
        InboxItem updated = existing.withStatus(status);
        items.put(idempotencyKey, updated);
        return updated;
    }

    /**
     * Returns all items matching the given status.
     *
     * @param status the status to filter by
     * @return a stream of matching inbox items
     */
    @Override
    public Stream<InboxItem> findByStatus(InboxItemStatus status) {
        return items.values().stream()
                .filter(item -> item.status() == status);
    }
}
