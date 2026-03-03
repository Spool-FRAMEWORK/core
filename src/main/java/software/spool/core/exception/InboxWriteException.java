package software.spool.core.exception;

public class InboxWriteException extends SpoolException {
    public InboxWriteException(String message) {
        super("Error occurred while writing to inbox: " + message);
    }
    public InboxWriteException(String message, Throwable cause) {
        super("Error occurred while writing to inbox: " + message, cause);
    }
}
