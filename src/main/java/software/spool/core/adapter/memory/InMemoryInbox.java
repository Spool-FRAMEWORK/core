package software.spool.core.adapter.memory;

import software.spool.core.exception.InboxReadException;
import software.spool.core.model.*;
import software.spool.core.model.vo.IdempotencyKey;
import software.spool.core.port.inbox.InboxUpdater;

import java.util.Optional;
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
    public InboxItem update(IdempotencyKey idempotencyKey, EnvelopeStatus status) {
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
    public Stream<InboxItem> findByStatus(EnvelopeStatus status) {
        return items.values().stream()
                .filter(item -> item.status() == status);
    }

    @Override
    public Optional<InboxItem> getBy(IdempotencyKey idempotencyKey) throws InboxReadException {
        return items.containsKey(idempotencyKey) ? Optional.of(items.get(idempotencyKey)) : Optional.empty();
    }
}
