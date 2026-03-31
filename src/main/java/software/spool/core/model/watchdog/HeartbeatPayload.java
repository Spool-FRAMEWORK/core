package software.spool.core.model.watchdog;

public record HeartbeatPayload(
    String moduleId,
    ModuleStatus status
) {}