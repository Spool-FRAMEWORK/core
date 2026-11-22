package software.spool.core.adapter.health;

import software.spool.core.model.vo.Envelope;
import software.spool.core.model.vo.IdempotencyKey;
import software.spool.core.port.health.PieceMonitor;
import software.spool.core.port.inbox.InboxEnvelopeRemover;

import java.util.Collection;

/**
 * Removes envelopes from the inbox through another remover and tells its {@link PieceMonitor} how each call went.
 */
public final class MonitoredInboxEnvelopeRemover implements InboxEnvelopeRemover {
    private final InboxEnvelopeRemover delegate;
    private final PieceMonitor monitor;

    public MonitoredInboxEnvelopeRemover(InboxEnvelopeRemover delegate, PieceMonitor monitor) {
        this.delegate = delegate;
        this.monitor = monitor;
    }

    @Override
    public Envelope remove(IdempotencyKey idempotencyKey) {
        return monitor.call(() -> delegate.remove(idempotencyKey));
    }

    @Override
    public Collection<Envelope> remove(Collection<IdempotencyKey> idempotencyKeys) {
        return monitor.call(() -> delegate.remove(idempotencyKeys));
    }
}
