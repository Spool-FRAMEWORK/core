package software.spool.core.port.quarantine;

import software.spool.core.exception.QuarantineConflictException;
import software.spool.core.exception.QuarantineRepositoryException;
import software.spool.core.model.quarantine.QuarantinePage;
import software.spool.core.model.quarantine.QuarantinePageRequest;
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
 * Resolving only replaces a record that is still waiting to be resolved, so two people resolving the same record at
 * once never both succeed.
 * </p>
 */
public interface QuarantineRepository {
    void save(QuarantinedRecord quarantinedRecord) throws QuarantineRepositoryException;

    /**
     * Saves a record that ends its current stay, only if the kept record is still {@link QuarantineStatus#PENDING}.
     *
     * @throws QuarantineConflictException if the key is not in quarantine or its record is already resolved
     */
    void resolve(QuarantinedRecord resolvedRecord) throws QuarantineConflictException, QuarantineRepositoryException;

    Optional<QuarantinedRecord> find(IdempotencyKey idempotencyKey) throws QuarantineRepositoryException;

    Collection<QuarantinedRecord> findByStatus(QuarantineStatus status) throws QuarantineRepositoryException;

    /**
     * One page of the records in a status, in the order of their keys.
     */
    QuarantinePage findByStatus(QuarantinePageRequest request) throws QuarantineRepositoryException;

    Collection<QuarantinedRecord> findAll() throws QuarantineRepositoryException;

    void delete(IdempotencyKey idempotencyKey) throws QuarantineRepositoryException;
}
