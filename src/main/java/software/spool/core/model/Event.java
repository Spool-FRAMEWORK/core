package software.spool.core.model;

import java.time.Instant;

public interface Event {
    String eventId();
    String causationId();
    String correlationId();
    Instant timestamp();
}