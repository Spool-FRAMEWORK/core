package software.spool.core.port.watchdog;

import software.spool.core.model.watchdog.ModuleStatus;

public interface WatchdogHeartBeat {
    boolean beat(String moduleId, ModuleStatus status);
}
