package software.spool.core.exception;

import software.spool.core.model.SpoolEvent;

/**
 * Wraps an exception together with the {@link SpoolEvent} that was being
 * processed when the error occurred, preserving context for error routing.
 */
public class SpoolContextException extends RuntimeException {
    private final SpoolEvent context;

    public SpoolContextException(Exception cause, SpoolEvent context) {
        super(cause);
        this.context = context;
    }

    public SpoolEvent context() {
        return context;
    }

    public Exception original() {
        return (Exception) getCause();
    }
}