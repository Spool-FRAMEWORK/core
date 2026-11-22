package software.spool.core.exception;

/**
 * Thrown when a mount result taken out of quarantine still cannot be written to its data mart.
 */
public class QuarantinedResultRejectedException extends SpoolException {
    public QuarantinedResultRejectedException(String message, Throwable cause) {
        super(message, cause);
    }
}
