package software.spool.core.exception;

/**
 * Thrown when an envelope cannot be removed from the bronze layer.
 */
public class BronzeRemovalException extends SpoolException {
    public BronzeRemovalException(String message, Throwable cause) {
        super(message, cause);
    }
}
