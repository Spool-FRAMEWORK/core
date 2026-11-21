package software.spool.core.exception;

import software.spool.core.model.vo.IdempotencyKey;

/**
 * Thrown when a quarantine record cannot be resolved because it is no longer waiting to be: someone else resolved it
 * first, or it is not in quarantine.
 */
public class QuarantineConflictException extends SpoolException {
    private final IdempotencyKey idempotencyKey;

    public QuarantineConflictException(IdempotencyKey idempotencyKey, String message) {
        super(message);
        this.idempotencyKey = idempotencyKey;
    }

    public IdempotencyKey idempotencyKey() {
        return idempotencyKey;
    }
}
