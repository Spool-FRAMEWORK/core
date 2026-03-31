package software.spool.core.port.watchdog;

import software.spool.core.model.watchdog.ModuleHealthView;

import java.util.Collection;

public interface WatchdogHealthQuery {
    Collection<ModuleHealthView> query();
}
