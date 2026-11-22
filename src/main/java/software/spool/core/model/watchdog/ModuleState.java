package software.spool.core.model.watchdog;

import software.spool.core.port.health.PieceHealth;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * What the watchdog knows of a module: when it last heard from it, how it was doing and how each piece of
 * infrastructure it uses was doing.
 */
public record ModuleState(
        ModuleIdentity identity,
        Instant lastSeen,
        ModuleStatus status,
        List<PieceHealth> pieces
) {
    public ModuleState {
        Objects.requireNonNull(identity);
        lastSeen = Objects.requireNonNullElse(lastSeen, Instant.now());
        status = Objects.requireNonNullElse(status, ModuleStatus.HEALTHY);
        pieces = List.copyOf(Objects.requireNonNullElse(pieces, List.of()));
    }

    public static ModuleState of(ModuleIdentity identity) {
        return new ModuleState(identity, Instant.now(), ModuleStatus.HEALTHY, List.of());
    }

    public ModuleState seenNow() {
        return new ModuleState(identity, Instant.now(), status, pieces);
    }

    public ModuleState status(ModuleStatus status) {
        return new ModuleState(identity, lastSeen, status, pieces);
    }

    public ModuleState pieces(List<PieceHealth> pieces) {
        return new ModuleState(identity, lastSeen, status, pieces);
    }
}
