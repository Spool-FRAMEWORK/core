package software.spool.core.adapter.health;

import software.spool.core.exception.EnvelopeHeaderStoreException;
import software.spool.core.model.vo.EnvelopeHeader;
import software.spool.core.model.vo.PartitionKey;
import software.spool.core.port.datalake.EnvelopeHeaderWriter;
import software.spool.core.port.health.PieceMonitor;

/**
 * Keeps envelope headers through another writer and tells its {@link PieceMonitor} how each call went.
 */
public final class MonitoredEnvelopeHeaderWriter implements EnvelopeHeaderWriter {
    private final EnvelopeHeaderWriter delegate;
    private final PieceMonitor monitor;

    public MonitoredEnvelopeHeaderWriter(EnvelopeHeaderWriter delegate, PieceMonitor monitor) {
        this.delegate = delegate;
        this.monitor = monitor;
    }

    @Override
    public void write(PartitionKey partition, EnvelopeHeader header) throws EnvelopeHeaderStoreException {
        EnvelopeHeaderStoreCalls.call(() -> monitor.call(() -> {
            delegate.write(partition, header);
            return null;
        }), "Keeping the header of envelope " + header.idempotencyKey().value());
    }
}
