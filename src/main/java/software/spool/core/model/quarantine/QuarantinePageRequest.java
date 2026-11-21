package software.spool.core.model.quarantine;

import software.spool.core.model.vo.IdempotencyKey;

import java.util.Objects;
import java.util.Optional;

/**
 * Asks for the records in one status, in the order of their keys: at most {@code limit} of them, after the key
 * {@code after} when it is given.
 */
public record QuarantinePageRequest(QuarantineStatus status, int limit, Optional<IdempotencyKey> after) {
    public QuarantinePageRequest {
        Objects.requireNonNull(status, "status is required");
        Objects.requireNonNull(after, "after is required");
        if (limit < 1) throw new IllegalArgumentException("A page needs at least one record; the limit given is " + limit);
    }

    public static QuarantinePageRequest first(QuarantineStatus status, int limit) {
        return new QuarantinePageRequest(status, limit, Optional.empty());
    }
}
