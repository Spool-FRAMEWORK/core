package software.spool.core.model.event;

import software.spool.core.model.SpoolEvent;
import software.spool.core.model.watchdog.ModuleIdentity;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record ModuleHeartbeat(
        String eventId,
        String causationId,
        String correlationId,
        Instant timestamp,
        ModuleIdentity identity) implements SpoolEvent {
    public ModuleHeartbeat {
        Objects.requireNonNull(eventId, "eventId is required");
        Objects.requireNonNull(timestamp, "timestamp is required");
        Objects.requireNonNull(identity, "identity is required");
    }

    public static ModuleHeartbeat.Builder builder() {
        return new ModuleHeartbeat.Builder();
    }

    public static class Builder {
        private ModuleIdentity identity;

        public ModuleHeartbeat.Builder identity(final ModuleIdentity identity) {
            this.identity = identity;
            return this;
        }

        public ModuleHeartbeat build() {
            return new ModuleHeartbeat(
                    UUID.randomUUID().toString(),
                    null,
                    null,
                    Instant.now(),
                    identity);
        }
    }
}
