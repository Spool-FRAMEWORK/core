package software.spool.core.model;

import java.time.Instant;

public interface SpoolEvent {
    String eventId();
    String eventType();
    Instant timestamp();
}
