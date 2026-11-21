package software.spool.core.port.datalake;

import software.spool.core.exception.BronzeRemovalException;
import software.spool.core.model.vo.IdempotencyKey;
import software.spool.core.model.vo.PartitionKey;

/**
 * Removes envelopes from the bronze layer by their partition and their idempotency key.
 *
 * <p>
 * Removing an envelope that does not exist does nothing.
 * </p>
 */
public interface BronzeEnvelopeRemover {
    void remove(PartitionKey partition, IdempotencyKey idempotencyKey) throws BronzeRemovalException;
}
