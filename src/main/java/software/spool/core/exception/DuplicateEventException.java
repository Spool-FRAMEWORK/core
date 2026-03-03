package software.spool.core.exception;

public class DuplicateEventException extends SpoolException {
    private final String idempotencyKey;

    public DuplicateEventException(String idempotencyKey) {
        super("Duplicate event: " + idempotencyKey);
        this.idempotencyKey = idempotencyKey;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }
}