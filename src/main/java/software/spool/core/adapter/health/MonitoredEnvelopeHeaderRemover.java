package software.spool.core.adapter.health;

import software.spool.core.exception.EnvelopeHeaderStoreException;
import software.spool.core.model.vo.IdempotencyKey;
import software.spool.core.model.vo.PartitionKey;
import software.spool.core.port.datalake.EnvelopeHeaderRemover;
import software.spool.core.port.health.PieceMonitor;

/**
 * Removes envelope headers through another remover and tells its {@link PieceMonitor} how each call went.
 */
public final class MonitoredEnvelopeHeaderRemover implements EnvelopeHeaderRemover {
    private final EnvelopeHeaderRemover delegate;
    private final PieceMonitor monitor;

    public MonitoredEnvelopeHeaderRemover(EnvelopeHeaderRemover delegate, PieceMonitor monitor) {
        this.delegate = delegate;
        this.monitor = monitor;
    }

    @Override
    public void remove(PartitionKey partition, IdempotencyKey idempotencyKey) throws EnvelopeHeaderStoreException {
        EnvelopeHeaderStoreCalls.call(() -> monitor.call(() -> {
            delegate.remove(partition, idempotencyKey);
            return null;
        }), "Removing the header of envelope " + idempotencyKey.value());
    }
}
