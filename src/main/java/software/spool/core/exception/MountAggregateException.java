package software.spool.core.exception;

/**
 * Thrown when writing a new item to the inbox fails.
 */
public class MountAggregateException extends SpoolException {
    public MountAggregateException(String message) {
        super("Error occurred while aggregating: " + message);
    }
}
