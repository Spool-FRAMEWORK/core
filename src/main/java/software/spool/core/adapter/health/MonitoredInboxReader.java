package software.spool.core.adapter.health;

import software.spool.core.exception.InboxReadException;
import software.spool.core.model.EnvelopeStatus;
import software.spool.core.model.vo.Envelope;
import software.spool.core.model.vo.IdempotencyKey;
import software.spool.core.port.health.PieceMonitor;
import software.spool.core.port.inbox.InboxReader;

import java.time.Instant;
import java.util.Collection;
import java.util.Optional;

/**
 * Reads the inbox through another reader and tells its {@link PieceMonitor} how each call went.
 */
public final class MonitoredInboxReader implements InboxReader {
    private final InboxReader delegate;
    private final PieceMonitor monitor;

    public MonitoredInboxReader(InboxReader delegate, PieceMonitor monitor) {
        this.delegate = delegate;
        this.monitor = monitor;
    }

    @Override
    public Collection<Envelope> findByStatus(EnvelopeStatus status) throws InboxReadException {
        return monitor.call(() -> delegate.findByStatus(status));
    }

    @Override
    public Collection<Envelope> findByStatusModifiedBefore(EnvelopeStatus status, Instant limit) throws InboxReadException {
        return monitor.call(() -> delegate.findByStatusModifiedBefore(status, limit));
    }

    @Override
    public Optional<Envelope> findById(IdempotencyKey idempotencyKey) throws InboxReadException {
        return monitor.call(() -> delegate.findById(idempotencyKey));
    }

    @Override
    public Collection<Envelope> findByIds(Collection<IdempotencyKey> idempotencyKeys) throws InboxReadException {
        return monitor.call(() -> delegate.findByIds(idempotencyKeys));
    }
}
