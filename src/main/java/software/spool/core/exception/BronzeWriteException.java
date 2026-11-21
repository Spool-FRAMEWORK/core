package software.spool.core.exception;

/**
 * Thrown when an envelope cannot be written to the bronze layer.
 */
public class BronzeWriteException extends SpoolException {
    public BronzeWriteException(String message, Throwable cause) {
        super(message, cause);
    }
}
