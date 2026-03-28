package software.spool.core.exception;

import software.spool.core.model.vo.IdempotencyKey;

/**
 * Thrown when an inbox write is rejected because an item with the same
 * {@link IdempotencyKey} already exists.
 */
public class DuplicateEventException extends SpoolException {
    private final IdempotencyKey idempotencyKey;

    public DuplicateEventException(IdempotencyKey idempotencyKey) {
        super("Duplicate event: " + idempotencyKey);
        this.idempotencyKey = idempotencyKey;
    }

    public IdempotencyKey getIdempotencyKey() {
        return idempotencyKey;
    }
}