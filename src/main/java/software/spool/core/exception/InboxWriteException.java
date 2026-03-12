package software.spool.core.exception;

/**
 * Thrown when writing a new item to the inbox fails.
 */
public class InboxWriteException extends SpoolException {
    public InboxWriteException(String message) {
        super("Error occurred while writing to inbox: " + message);
    }

    public InboxWriteException(String message, Throwable cause) {
        super("Error occurred while writing to inbox: " + message, cause);
    }
}
