package software.spool.core.port.watchdog;

import software.spool.core.model.watchdog.ModuleIdentity;

public interface WatchdogModuleRegistration {
    void register(ModuleIdentity identity);

}
