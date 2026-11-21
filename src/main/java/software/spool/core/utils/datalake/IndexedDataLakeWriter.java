package software.spool.core.utils.datalake;

import software.spool.core.adapter.logging.LoggerFactory;
import software.spool.core.exception.EnvelopeHeaderStoreException;
import software.spool.core.model.vo.IdempotencyKey;
import software.spool.core.model.vo.PartitionedEnvelope;
import software.spool.core.port.datalake.DataLakeWriter;
import software.spool.core.port.datalake.EnvelopeHeaderWriter;
import software.spool.core.port.logging.Logger;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Writes envelopes to the data lake and then keeps their headers in a store of their own. An envelope counts as
 * written only when both are done, so a header is never kept for an envelope the data lake does not have.
 */
public class IndexedDataLakeWriter implements DataLakeWriter {
    private static final Logger LOG = LoggerFactory.getLogger(IndexedDataLakeWriter.class);

    private final DataLakeWriter dataLake;
    private final EnvelopeHeaderWriter headers;

    public IndexedDataLakeWriter(DataLakeWriter dataLake, EnvelopeHeaderWriter headers) {
        this.dataLake = dataLake;
        this.headers = headers;
    }

    @Override
    public Stream<IdempotencyKey> write(Collection<PartitionedEnvelope> items) {
        Set<IdempotencyKey> written = dataLake.write(items).collect(Collectors.toSet());
        List<IdempotencyKey> indexed = items.stream()
                .filter(item -> written.contains(item.envelope().idempotencyKey()))
                .filter(this::keptHeader)
                .map(item -> item.envelope().idempotencyKey())
                .toList();
        return indexed.stream();
    }

    private boolean keptHeader(PartitionedEnvelope item) {
        try {
            headers.write(item.partition(), item.envelope().header());
            return true;
        } catch (EnvelopeHeaderStoreException e) {
            LOG.warn("Envelope {} is in data lake partition {} but its header could not be stored: {}",
                    item.envelope().idempotencyKey().value(), item.partition().value(), e.getMessage());
            return false;
        }
    }
}
