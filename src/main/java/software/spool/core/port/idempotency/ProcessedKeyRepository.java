package software.spool.core.port.idempotency;

import software.spool.core.model.vo.IdempotencyKey;

/**
 * The idempotency keys a consumer already processed, remembered during a window (ADR-007). A consumer saves a key
 * only after it processed its event successfully, and does not repeat the work for a key it contains.
 */
public interface ProcessedKeyRepository {
    boolean contains(IdempotencyKey key);

    /** Remembers the key for a whole window from now, also when it was already remembered. */
    void save(IdempotencyKey key);
}
