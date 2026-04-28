package software.spool.core.exception;

import software.spool.core.model.vo.IdempotencyKey;

import java.util.Collection;
import java.util.List;

public class InboxUpdateException extends SpoolException {
    private final Collection<IdempotencyKey> idempotencyKeys;

    public InboxUpdateException(Collection<IdempotencyKey> idempotencyKeys, String message) {
        super("Error occurred while updating from inbox: " + idempotencyKeys.stream().map(IdempotencyKey::value) + ". " + message);
        this.idempotencyKeys = idempotencyKeys;
    }

    public InboxUpdateException(IdempotencyKey idempotencyKey, String message) {
        super("Error occurred while updating from inbox: " + idempotencyKey.value() + ". " + message);
        this.idempotencyKeys = List.of(idempotencyKey);
    }

    public InboxUpdateException(Collection<IdempotencyKey> idempotencyKeys, String message, Throwable cause) {
        super("Error occurred while updating from inbox: " + idempotencyKeys.stream().map(IdempotencyKey::value) + ". " + message, cause);
        this.idempotencyKeys = idempotencyKeys;
    }

    public InboxUpdateException(IdempotencyKey idempotencyKey, String message, Throwable cause) {
        super("Error occurred while updating from inbox: " + idempotencyKey.value() + ". " + message, cause);
        this.idempotencyKeys = List.of(idempotencyKey);
    }

    public Collection<IdempotencyKey> getIdempotencyKeys() {
        return idempotencyKeys;
    }
}
