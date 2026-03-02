package software.spool.core.model;

import java.time.Instant;

public interface SpoolEvent {
    String eventId();
    Instant timestamp();
    String correlationId();
    String causationId();
}
