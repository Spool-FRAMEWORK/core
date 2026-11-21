package software.spool.core.port.datalake;

import software.spool.core.exception.BronzeWriteException;
import software.spool.core.model.vo.Envelope;
import software.spool.core.model.vo.PartitionKey;

/**
 * Writes whole envelopes to the bronze layer, each one found by its partition and its idempotency key.
 *
 * <p>
 * Saving an envelope that already exists replaces it.
 * </p>
 */
public interface BronzeWriter {
    void save(PartitionKey partition, Envelope envelope) throws BronzeWriteException;
}
