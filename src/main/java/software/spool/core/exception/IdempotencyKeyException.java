package software.spool.core.exception;

import software.spool.core.model.vo.IdempotencyKeySchema;

/**
 * Thrown when a record lacks a field its {@link IdempotencyKeySchema} builds the key from, and the schema says to
 * fail instead of keying it by the whole record.
 */
public class IdempotencyKeyException extends SpoolException {
    private final String sourceId;
    private final String field;

    public IdempotencyKeyException(String sourceId, String field) {
        super("Record of source " + sourceId + " not captured: it has no value for the field " + field
                + " of its idempotency key schema");
        this.sourceId = sourceId;
        this.field = field;
    }

    public String sourceId() {
        return sourceId;
    }

    public String field() {
        return field;
    }
}
