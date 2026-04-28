package software.spool.core.adapter.memory;

import software.spool.core.exception.InboxReadException;
import software.spool.core.exception.InboxUpdateException;
import software.spool.core.model.*;
import software.spool.core.model.vo.Envelope;
import software.spool.core.model.vo.IdempotencyKey;
import software.spool.core.port.inbox.InboxEnvelopeResolver;
import software.spool.core.port.inbox.InboxStatusQuery;
import software.spool.core.port.inbox.InboxUpdater;

import java.util.Collection;
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
public class InMemoryInbox implements InboxUpdater, InboxEnvelopeResolver, InboxStatusQuery {
    private final ConcurrentHashMap<IdempotencyKey, Envelope> envelopes = new ConcurrentHashMap<>();

    @Override
    public Optional<Envelope> findById(IdempotencyKey idempotencyKey) throws InboxReadException {
        return Optional.ofNullable(envelopes.get(idempotencyKey));
    }

    @Override
    public Collection<Envelope> findByIds(Collection<IdempotencyKey> idempotencyKeys) throws InboxReadException {
        return idempotencyKeys.stream().map(this::findById).filter(Optional::isPresent).map(Optional::get).toList();
    }

    @Override
    public Collection<Envelope> findByStatus(EnvelopeStatus status) throws InboxReadException {
        return this.envelopes.values().stream().filter(e -> e.status().equals(status)).toList();
    }

    @Override
    public Envelope update(Envelope envelope) throws InboxUpdateException {
        return update(envelope.idempotencyKey(), envelope.status());
    }

    @Override
    public Envelope update(IdempotencyKey idempotencyKey, EnvelopeStatus status) throws InboxUpdateException {
        return envelopes.put(idempotencyKey, envelopes.get(idempotencyKey).withStatus(status));
    }
}
