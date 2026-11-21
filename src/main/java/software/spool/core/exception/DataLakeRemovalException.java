package software.spool.core.exception;

/**
 * Thrown when an envelope cannot be removed from the data lake.
 */
public class DataLakeRemovalException extends SpoolException {
    public DataLakeRemovalException(String message, Throwable cause) {
        super(message, cause);
    }
}
