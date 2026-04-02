package software.spool.core.model.watchdog;

public record HeartbeatPayload(
    ModuleIdentity identity,
    ModuleStatus status
) {}