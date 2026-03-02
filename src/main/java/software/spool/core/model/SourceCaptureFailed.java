package software.spool.core.model;

import java.time.Instant;
import java.util.UUID;

public record SourceCaptureFailed(
        String eventId,
        String eventType,
        Instant timestamp,
        String errorMessage
) implements SpoolEvent {
    public static SourceCaptureFailed with(String errorMessage) {
        return new SourceCaptureFailed(UUID.randomUUID().toString(), "SOURCE_FAILED", Instant.now(), errorMessage);
    }
}
