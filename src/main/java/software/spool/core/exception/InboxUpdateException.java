package software.spool.core.exception;

import software.spool.core.model.IdempotencyKey;

public class InboxUpdateException extends SpoolException {
    private final IdempotencyKey idempotencyKey;

    public InboxUpdateException(IdempotencyKey idempotencyKey, String message) {
        super("Error occurred while updating from inbox: " + idempotencyKey.value() + ". " + message);
        this.idempotencyKey = idempotencyKey;
    }
    public InboxUpdateException(IdempotencyKey idempotencyKey, String message, Throwable cause) {
        super("Error occurred while updating from inbox: " + idempotencyKey.value() + ". " + message, cause);
        this.idempotencyKey = idempotencyKey;
    }

    public IdempotencyKey getIdempotencyKey() {
        return idempotencyKey;
    }
}
