package software.spool.core.model;

public interface SpoolFailedEvent extends SpoolEvent {
    String errorMessage();
}
