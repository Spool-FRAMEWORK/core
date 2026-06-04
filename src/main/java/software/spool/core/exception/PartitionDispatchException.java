package software.spool.core.exception;

public class PartitionDispatchException extends SpoolException {
    public PartitionDispatchException(String message) {
        super("Partition dispatch failed: " + message);
    }

    public PartitionDispatchException(String message, Throwable cause) {
        super("Partition dispatch failed: " + message, cause);
    }
}
