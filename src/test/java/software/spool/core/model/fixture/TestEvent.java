package software.spool.core.model.fixture;

import software.spool.core.model.SpoolEvent;

import java.time.Instant;

public record TestEvent(
        String eventId,
        String causationId,
        String correlationId,
        Instant timestamp
) implements SpoolEvent {
    public static TestEvent sample() {
        return new TestEvent("evt-1", null, null, Instant.now());
    }
}
