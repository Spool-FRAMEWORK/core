package software.spool.core.model.schedule;

public class InvalidScheduleException extends IllegalArgumentException {
    public InvalidScheduleException(String message) {
        super(message);
    }
}
