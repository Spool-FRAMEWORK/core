package software.spool.core.model.watchdog;

import software.spool.core.port.health.PieceHealth;

import java.util.List;
import java.util.Objects;

/**
 * What a module tells the watchdog on each beat: who it is, how it is doing and how each piece of infrastructure it
 * uses is doing. A beat from a module that does not report its pieces has none.
 */
public record HeartbeatPayload(
    ModuleIdentity identity,
    ModuleStatus status,
    List<PieceHealth> pieces
) {
    public HeartbeatPayload {
        pieces = List.copyOf(Objects.requireNonNullElse(pieces, List.of()));
    }
}
