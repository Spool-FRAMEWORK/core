package software.spool.core.exception;

import software.spool.core.model.SpoolEvent;

public class SpoolContextException extends RuntimeException {
    private final SpoolEvent context;

    public SpoolContextException(Exception cause, SpoolEvent context) {
        super(cause);
        this.context = context;
    }

    public SpoolEvent context() { return context; }
    public Exception original() { return (Exception) getCause(); }
}