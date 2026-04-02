package software.spool.core.model.watchdog;

import java.time.Instant;
import java.util.Objects;

public record ModuleState(
        ModuleIdentity identity,
        Instant lastSeen,
        ModuleStatus status
) {
    public ModuleState {
        Objects.requireNonNull(identity);
        lastSeen = Objects.requireNonNullElse(lastSeen, Instant.now());
        status = Objects.requireNonNullElse(status, ModuleStatus.HEALTHY);
    }

    public static ModuleState of(ModuleIdentity identity) {
        return new ModuleState(identity, Instant.now(), ModuleStatus.HEALTHY);
    }

    public ModuleState seenNow() {
        return ModuleState.of(identity);
    }

    public ModuleState status(ModuleStatus status) {
        return new ModuleState(identity, lastSeen, status);
    }
}