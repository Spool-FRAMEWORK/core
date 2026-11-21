package software.spool.core.utils.datalake;

import software.spool.core.exception.DataLakeRemovalException;
import software.spool.core.exception.EnvelopeHeaderStoreException;
import software.spool.core.model.vo.IdempotencyKey;
import software.spool.core.model.vo.PartitionKey;
import software.spool.core.port.datalake.DataLakeEnvelopeRemover;
import software.spool.core.port.datalake.EnvelopeHeaderRemover;

/**
 * Removes the header of an envelope from its store and then the envelope from the data lake, so the store never
 * keeps a header of an envelope the data lake no longer has.
 */
public class IndexedDataLakeEnvelopeRemover implements DataLakeEnvelopeRemover {
    private final EnvelopeHeaderRemover headers;
    private final DataLakeEnvelopeRemover dataLake;

    public IndexedDataLakeEnvelopeRemover(EnvelopeHeaderRemover headers, DataLakeEnvelopeRemover dataLake) {
        this.headers = headers;
        this.dataLake = dataLake;
    }

    @Override
    public void remove(PartitionKey partition, IdempotencyKey idempotencyKey) {
        try {
            headers.remove(partition, idempotencyKey);
        } catch (EnvelopeHeaderStoreException e) {
            throw new DataLakeRemovalException("Header of envelope " + idempotencyKey.value()
                    + " could not be removed from its store, so it stays in data lake partition " + partition.value(), e);
        }
        dataLake.remove(partition, idempotencyKey);
    }
}
