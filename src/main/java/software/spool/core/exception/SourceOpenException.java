package software.spool.core.exception;

public class SourceOpenException extends SpoolException {
    public SourceOpenException(String message) {
        super(message);
    }
    public SourceOpenException(String message, Throwable cause) {
        super(message, cause);
    }
}
