package software.spool.core.port.quarantine;

import software.spool.core.exception.QuarantineRepositoryException;
import software.spool.core.model.quarantine.QuarantineStatus;
import software.spool.core.model.quarantine.QuarantinedRecord;
import software.spool.core.model.vo.IdempotencyKey;

import java.util.Collection;
import java.util.Optional;

/**
 * Keeps one record per envelope in quarantine, found by the envelope's idempotency key or by its status.
 *
 * <p>
 * Saving a record for a key that already has one replaces it. Deleting a key that has no record does nothing.
 * </p>
 */
public interface QuarantineRepository {
    void save(QuarantinedRecord quarantinedRecord) throws QuarantineRepositoryException;

    Optional<QuarantinedRecord> find(IdempotencyKey idempotencyKey) throws QuarantineRepositoryException;

    Collection<QuarantinedRecord> findByStatus(QuarantineStatus status) throws QuarantineRepositoryException;

    Collection<QuarantinedRecord> findAll() throws QuarantineRepositoryException;

    void delete(IdempotencyKey idempotencyKey) throws QuarantineRepositoryException;
}
