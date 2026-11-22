package software.spool.core.adapter.health;

import software.spool.core.exception.EnvelopeHeaderStoreException;
import software.spool.core.model.vo.EnvelopeHeader;
import software.spool.core.model.vo.IdempotencyKey;
import software.spool.core.model.vo.PartitionKey;
import software.spool.core.port.datalake.EnvelopeHeaderReader;
import software.spool.core.port.health.PieceMonitor;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Finds envelope headers through another reader and tells its {@link PieceMonitor} how each call went, including
 * each header read from a partition.
 */
public final class MonitoredEnvelopeHeaderReader implements EnvelopeHeaderReader {
    private final EnvelopeHeaderReader delegate;
    private final PieceMonitor monitor;

    public MonitoredEnvelopeHeaderReader(EnvelopeHeaderReader delegate, PieceMonitor monitor) {
        this.delegate = delegate;
        this.monitor = monitor;
    }

    @Override
    public Optional<EnvelopeHeader> find(IdempotencyKey idempotencyKey) throws EnvelopeHeaderStoreException {
        return EnvelopeHeaderStoreCalls.call(() -> monitor.call(() -> delegate.find(idempotencyKey)),
                "Finding the header of envelope " + idempotencyKey.value());
    }

    @Override
    public Stream<EnvelopeHeader> findIn(PartitionKey partition) throws EnvelopeHeaderStoreException {
        return EnvelopeHeaderStoreCalls.call(() -> monitor.stream(() -> delegate.findIn(partition)),
                "Finding the headers of partition " + partition.value());
    }
}
