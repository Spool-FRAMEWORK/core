package software.spool.core.port.watchdog;

import software.spool.core.model.watchdog.ModuleIdentity;
import software.spool.core.model.watchdog.ModuleStatus;

public interface WatchdogHeartBeat {
    void beat(ModuleIdentity identity, ModuleStatus status);
}
