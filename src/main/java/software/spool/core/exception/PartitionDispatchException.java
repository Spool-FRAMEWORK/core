package software.spool.core.exception;

public class PartitionDispatchException extends SpoolException {
    public PartitionDispatchException(String message) {
        super("Partition dispatch failed: " + message);
    }
}
