package software.spool.core.model.watchdog;

import java.time.Duration;

public record ModuleIdentity(
    String moduleId,
    Duration heartbeatInterval,
    Duration timeout
) {}