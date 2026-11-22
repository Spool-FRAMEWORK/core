package software.spool.core.port.watchdog;

import software.spool.core.model.watchdog.ModuleIdentity;
import software.spool.core.port.health.ModuleHealthPayload;

public interface WatchdogHeartBeat {
    void beat(ModuleIdentity identity, ModuleHealthPayload health);
}
