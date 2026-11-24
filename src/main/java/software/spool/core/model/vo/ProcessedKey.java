package software.spool.core.model.vo;

import java.time.Instant;
import java.util.Objects;

/**
 * The idempotency key of a record that was already processed, with the moment it was (ADR-033). Whoever remembers it
 * decides, from that moment, when to forget it.
 */
public record ProcessedKey(IdempotencyKey key, Instant processedAt) {
    public ProcessedKey {
        Objects.requireNonNull(key, "key is required");
        Objects.requireNonNull(processedAt, "processedAt is required");
    }
}
