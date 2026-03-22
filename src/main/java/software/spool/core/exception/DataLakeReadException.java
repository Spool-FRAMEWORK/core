package software.spool.core.exception;

import software.spool.core.model.PartitionKey;

/**
 * Thrown when writing a new item to the inbox fails.
 */
public class DataLakeReadException extends SpoolException {
    public DataLakeReadException(PartitionKey partitionKey, String message) {
        super("Error occurred while reading from data lake (" + partitionKey + "): " + message);
    }

    public DataLakeReadException(PartitionKey partitionKey, String message, Throwable cause) {
        super("Error occurred while reading from data lake (" + partitionKey + "): " + message,  cause);
    }
}
