package software.spool.core.port.watchdog;

import software.spool.core.model.watchdog.ModuleIdentity;

import java.time.Duration;

public interface ModuleLogger {
    void moduleStarted(ModuleIdentity identity);
    void moduleStopped(ModuleIdentity identity, String reason);
    void moduleDegraded(ModuleIdentity identity, String reason);
    void moduleRecovered(ModuleIdentity identity, Duration downtime);
}