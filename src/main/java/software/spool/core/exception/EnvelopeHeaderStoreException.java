package software.spool.core.exception;

/**
 * Thrown when a store of envelope headers cannot keep, find or remove a header.
 */
public class EnvelopeHeaderStoreException extends SpoolException {
    public EnvelopeHeaderStoreException(String message, Throwable cause) {
        super(message, cause);
    }
}
