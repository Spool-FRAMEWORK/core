package software.spool.core.port.datalake;

import software.spool.core.exception.BronzeRepositoryException;
import software.spool.core.model.vo.Envelope;
import software.spool.core.model.vo.IdempotencyKey;
import software.spool.core.model.vo.PartitionKey;

/**
 * The records of the bronze layer, each one a whole envelope found by its partition and its idempotency key.
 *
 * <p>
 * Saving a record that already exists replaces it. Deleting a record that does not exist does nothing.
 * </p>
 */
public interface BronzeRepository {
    void save(PartitionKey partition, Envelope envelope) throws BronzeRepositoryException;

    void delete(PartitionKey partition, IdempotencyKey idempotencyKey) throws BronzeRepositoryException;
}
