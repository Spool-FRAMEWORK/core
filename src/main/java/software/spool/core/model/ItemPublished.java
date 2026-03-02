package software.spool.core.model;

import java.time.Instant;
import java.util.UUID;

public record ItemPublished(
        String eventId,
        String eventType,
        Instant timestamp,
        String payload
) implements SpoolEvent {
    public static ItemPublished with(String payload) {
        return new ItemPublished(UUID.randomUUID().toString(), "RAW_DATA_PUBLISHED", Instant.now(), payload);
    }
}
