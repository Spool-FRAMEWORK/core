package software.spool.core.port.watchdog;

import software.spool.core.model.watchdog.ModuleIdentity;

public interface ModuleLogger {
    void moduleStarted(ModuleIdentity identity);
    void moduleStopped(ModuleIdentity identity);
    void moduleDegraded(ModuleIdentity identity, String reason);
}