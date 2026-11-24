package software.spool.core.port.idempotency;

import software.spool.core.model.vo.IdempotencyKey;
import software.spool.core.model.vo.ProcessedKey;

import java.time.Instant;

/**
 * The idempotency keys already processed, each with the moment it was (ADR-007, ADR-033). The repository keeps and
 * finds them; whoever uses it decides from that moment when to forget them, with {@link #forgetProcessedBefore}.
 */
public interface ProcessedKeyRepository {
    boolean contains(IdempotencyKey key);

    /** Keeps the key with the moment it was processed, replacing the moment it had if it was already kept. */
    void save(ProcessedKey processedKey);

    /** Forgets the keys processed before the given moment; one processed at that very moment is kept. */
    void forgetProcessedBefore(Instant moment);
}
