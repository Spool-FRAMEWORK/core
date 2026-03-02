package software.spool.core.exception;

public class DuplicateEventException extends SpoolException {
    public DuplicateEventException(String key) {
        super("Duplicate event: " + key);
    }
}