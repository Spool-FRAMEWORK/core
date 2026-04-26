package software.spool.core.port.watchdog;

import software.spool.core.model.watchdog.ModuleState;

import java.util.Collection;

public interface WatchdogHealthQuery {
    Collection<ModuleState> query();
}
