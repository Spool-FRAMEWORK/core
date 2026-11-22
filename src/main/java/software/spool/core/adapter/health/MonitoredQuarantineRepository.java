package software.spool.core.adapter.health;

import software.spool.core.exception.QuarantineConflictException;
import software.spool.core.exception.QuarantineRepositoryException;
import software.spool.core.model.quarantine.QuarantinePage;
import software.spool.core.model.quarantine.QuarantinePageRequest;
import software.spool.core.model.quarantine.QuarantineStatus;
import software.spool.core.model.quarantine.QuarantinedRecord;
import software.spool.core.model.vo.IdempotencyKey;
import software.spool.core.port.health.PieceMonitor;
import software.spool.core.port.quarantine.QuarantineRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Keeps the quarantine through another repository and tells its {@link PieceMonitor} how each call went. A conflict
 * when resolving a record is an answer of the quarantine, not a failure of it.
 */
public final class MonitoredQuarantineRepository implements QuarantineRepository {
    private static final List<Class<? extends Exception>> ANSWERS = List.of(QuarantineConflictException.class);

    private final QuarantineRepository delegate;
    private final PieceMonitor monitor;

    public MonitoredQuarantineRepository(QuarantineRepository delegate, PieceMonitor monitor) {
        this.delegate = delegate;
        this.monitor = monitor;
    }

    @Override
    public void save(QuarantinedRecord quarantinedRecord) throws QuarantineRepositoryException {
        monitor.run(() -> delegate.save(quarantinedRecord));
    }

    @Override
    public void resolve(QuarantinedRecord resolvedRecord) throws QuarantineConflictException, QuarantineRepositoryException {
        monitor.call(() -> {
            delegate.resolve(resolvedRecord);
            return resolvedRecord;
        }, ANSWERS);
    }

    @Override
    public Optional<QuarantinedRecord> find(IdempotencyKey idempotencyKey) throws QuarantineRepositoryException {
        return monitor.call(() -> delegate.find(idempotencyKey));
    }

    @Override
    public Collection<QuarantinedRecord> findByStatus(QuarantineStatus status) throws QuarantineRepositoryException {
        return monitor.call(() -> delegate.findByStatus(status));
    }

    @Override
    public QuarantinePage findByStatus(QuarantinePageRequest request) throws QuarantineRepositoryException {
        return monitor.call(() -> delegate.findByStatus(request));
    }

    @Override
    public Collection<QuarantinedRecord> findAll() throws QuarantineRepositoryException {
        return monitor.call(delegate::findAll);
    }

    @Override
    public void delete(IdempotencyKey idempotencyKey) throws QuarantineRepositoryException {
        monitor.run(() -> delegate.delete(idempotencyKey));
    }
}
