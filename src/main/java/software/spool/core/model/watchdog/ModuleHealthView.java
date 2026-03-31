package software.spool.core.model.watchdog;

import java.time.Instant;

public record ModuleHealthView(
    String moduleId,
    ModuleStatus status,
    Instant lastSeen
) {}