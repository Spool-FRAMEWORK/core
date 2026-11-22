package software.spool.core.adapter.health;

import software.spool.core.exception.DataLakeRemovalException;
import software.spool.core.model.vo.IdempotencyKey;
import software.spool.core.model.vo.PartitionKey;
import software.spool.core.port.datalake.DataLakeEnvelopeRemover;
import software.spool.core.port.health.PieceMonitor;

/**
 * Removes envelopes from the data lake through another remover and tells its {@link PieceMonitor} how each call
 * went.
 */
public final class MonitoredDataLakeEnvelopeRemover implements DataLakeEnvelopeRemover {
    private final DataLakeEnvelopeRemover delegate;
    private final PieceMonitor monitor;

    public MonitoredDataLakeEnvelopeRemover(DataLakeEnvelopeRemover delegate, PieceMonitor monitor) {
        this.delegate = delegate;
        this.monitor = monitor;
    }

    @Override
    public void remove(PartitionKey partition, IdempotencyKey idempotencyKey) throws DataLakeRemovalException {
        monitor.run(() -> delegate.remove(partition, idempotencyKey));
    }
}
