package software.spool.core.adapter.health;

import software.spool.core.model.vo.IdempotencyKey;
import software.spool.core.model.vo.PartitionedEnvelope;
import software.spool.core.port.datalake.DataLakeWriter;
import software.spool.core.port.health.PieceMonitor;

import java.util.Collection;
import java.util.stream.Stream;

/**
 * Writes to the data lake through another writer and tells its {@link PieceMonitor} how each write went, also
 * while the keys it wrote are read.
 */
public final class MonitoredDataLakeWriter implements DataLakeWriter {
    private final DataLakeWriter delegate;
    private final PieceMonitor monitor;

    public MonitoredDataLakeWriter(DataLakeWriter delegate, PieceMonitor monitor) {
        this.delegate = delegate;
        this.monitor = monitor;
    }

    @Override
    public Stream<IdempotencyKey> write(Collection<PartitionedEnvelope> items) {
        return monitor.stream(() -> delegate.write(items));
    }
}
