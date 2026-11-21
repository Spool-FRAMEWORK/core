package software.spool.core.adapter.memory;

import software.spool.core.exception.QuarantineConflictException;
import software.spool.core.model.quarantine.QuarantinePage;
import software.spool.core.model.quarantine.QuarantinePageRequest;
import software.spool.core.model.quarantine.QuarantineStatus;
import software.spool.core.model.quarantine.QuarantinedRecord;
import software.spool.core.model.vo.IdempotencyKey;
import software.spool.core.port.quarantine.QuarantineRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@link QuarantineRepository} kept in memory, for tests and single-process runs. Its records are lost when the
 * process stops.
 */
public class InMemoryQuarantineRepository implements QuarantineRepository {
    private final ConcurrentHashMap<IdempotencyKey, QuarantinedRecord> records = new ConcurrentHashMap<>();

    @Override
    public void save(QuarantinedRecord quarantinedRecord) {
        records.put(quarantinedRecord.idempotencyKey(), quarantinedRecord);
    }

    @Override
    public void resolve(QuarantinedRecord resolvedRecord) {
        records.compute(resolvedRecord.idempotencyKey(), (key, kept) -> {
            if (kept == null || kept.status() != QuarantineStatus.PENDING) throw notPending(key, kept);
            return resolvedRecord;
        });
    }

    @Override
    public Optional<QuarantinedRecord> find(IdempotencyKey idempotencyKey) {
        return Optional.ofNullable(records.get(idempotencyKey));
    }

    @Override
    public Collection<QuarantinedRecord> findByStatus(QuarantineStatus status) {
        return records.values().stream().filter(quarantinedRecord -> quarantinedRecord.status() == status).toList();
    }

    @Override
    public QuarantinePage findByStatus(QuarantinePageRequest request) {
        return QuarantinePage.of(records.values(), request);
    }

    @Override
    public Collection<QuarantinedRecord> findAll() {
        return List.copyOf(records.values());
    }

    @Override
    public void delete(IdempotencyKey idempotencyKey) {
        records.remove(idempotencyKey);
    }

    private static QuarantineConflictException notPending(IdempotencyKey idempotencyKey, QuarantinedRecord kept) {
        return new QuarantineConflictException(idempotencyKey, kept == null
                ? idempotencyKey.value() + " is not in quarantine"
                : idempotencyKey.value() + " was already resolved as " + kept.status());
    }
}
