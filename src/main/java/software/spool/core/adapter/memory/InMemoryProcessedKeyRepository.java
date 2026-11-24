package software.spool.core.adapter.memory;

import software.spool.core.model.vo.IdempotencyKey;
import software.spool.core.model.vo.ProcessedKey;
import software.spool.core.port.idempotency.ProcessedKeyRepository;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Keeps the processed keys in the memory of the process, so they are lost when it restarts and each replica has its
 * own.
 */
public final class InMemoryProcessedKeyRepository implements ProcessedKeyRepository {
    private final Map<IdempotencyKey, Instant> processedAt = new HashMap<>();

    @Override
    public synchronized boolean contains(IdempotencyKey key) {
        return processedAt.containsKey(key);
    }

    @Override
    public synchronized void save(ProcessedKey processedKey) {
        processedAt.put(processedKey.key(), processedKey.processedAt());
    }

    @Override
    public synchronized void forgetProcessedBefore(Instant moment) {
        processedAt.values().removeIf(at -> at.isBefore(moment));
    }
}
