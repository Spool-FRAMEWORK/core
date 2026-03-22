package software.spool.core.exception;

import software.spool.core.model.PartitionKey;

/**
 * Thrown when writing a new item to the inbox fails.
 */
public class DataMartWriteException extends SpoolException {
    public DataMartWriteException(PartitionKey partitionKey, String message) {
        super("Error occurred while writing to data mart (" + partitionKey + "): " + message);
    }

    public DataMartWriteException(PartitionKey partitionKey, String message, Throwable cause) {
        super("Error occurred while writing to data mart (" + partitionKey + "): " + message, cause);
    }
}
