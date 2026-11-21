package software.spool.core.port.datalake;

import software.spool.core.exception.EnvelopeHeaderStoreException;
import software.spool.core.model.vo.EnvelopeHeader;
import software.spool.core.model.vo.PartitionKey;

/**
 * Keeps the header of an envelope written to a partition of the data lake. Writing the same header again replaces it.
 */
@FunctionalInterface
public interface EnvelopeHeaderWriter {
    void write(PartitionKey partition, EnvelopeHeader header) throws EnvelopeHeaderStoreException;
}
