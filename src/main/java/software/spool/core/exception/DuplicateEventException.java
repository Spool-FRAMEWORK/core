package software.spool.core.exception;

import software.spool.core.model.IdempotencyKey;

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