package software.spool.core.exception;

public class InboxWriteException extends SpoolException {
    public InboxWriteException(String message) {
        super(message);
    }
    public InboxWriteException(String message, Throwable cause) {
        super(message, cause);
    }
}
