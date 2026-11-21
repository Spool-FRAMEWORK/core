package software.spool.core.port.datalake;

import software.spool.core.exception.EnvelopeHeaderStoreException;
import software.spool.core.model.vo.IdempotencyKey;
import software.spool.core.model.vo.PartitionKey;

/**
 * Removes the header of an envelope from a partition. Removing a header that is not there does nothing.
 */
@FunctionalInterface
public interface EnvelopeHeaderRemover {
    void remove(PartitionKey partition, IdempotencyKey idempotencyKey) throws EnvelopeHeaderStoreException;
}
