package software.spool.core.adapter.watchdog;

public class WatchdogUnavailableException extends RuntimeException {
    public WatchdogUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}