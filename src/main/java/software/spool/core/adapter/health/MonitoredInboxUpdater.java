package software.spool.core.adapter.health;

import software.spool.core.exception.InboxUpdateException;
import software.spool.core.model.EnvelopeStatus;
import software.spool.core.model.vo.Envelope;
import software.spool.core.model.vo.IdempotencyKey;
import software.spool.core.port.health.PieceMonitor;
import software.spool.core.port.inbox.InboxUpdater;

import java.util.Collection;

/**
 * Updates the inbox through another updater and tells its {@link PieceMonitor} how each call went.
 */
public final class MonitoredInboxUpdater implements InboxUpdater {
    private final InboxUpdater delegate;
    private final PieceMonitor monitor;

    public MonitoredInboxUpdater(InboxUpdater delegate, PieceMonitor monitor) {
        this.delegate = delegate;
        this.monitor = monitor;
    }

    @Override
    public Envelope update(Envelope envelope) throws InboxUpdateException {
        return monitor.call(() -> delegate.update(envelope));
    }

    @Override
    public Envelope update(IdempotencyKey idempotencyKey, EnvelopeStatus status) throws InboxUpdateException {
        return monitor.call(() -> delegate.update(idempotencyKey, status));
    }

    @Override
    public Collection<Envelope> update(Collection<IdempotencyKey> idempotencyKeys, EnvelopeStatus status) throws InboxUpdateException {
        return monitor.call(() -> delegate.update(idempotencyKeys, status));
    }
}
