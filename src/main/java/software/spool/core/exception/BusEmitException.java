package software.spool.core.exception;

public class BusEmitException extends SpoolException {
    public BusEmitException(String message) {
        super("Error occurred while emitting to event bus: " + message);
    }
    public BusEmitException(String message, Throwable cause) {
        super("Error occurred while emitting to event bus: " + message, cause);
    }
}
