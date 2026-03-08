package software.spool.core.exception;

public class InboxReadException extends SpoolException {
    public InboxReadException(String message) {
        super("Error occurred while reading from inbox: " + message);
    }
    public InboxReadException(String message, Throwable cause) {
        super("Error occurred while reading from inbox: " + message, cause);
    }
}
