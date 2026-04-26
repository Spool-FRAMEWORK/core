package software.spool.core.port.watchdog;

import software.spool.core.model.watchdog.ModuleIdentity;

public interface ModuleHeartBeat {
    ModuleIdentity identity();
    void reportHealthy();
    void reportUnhealthy();
    void start();
    void stop();

    ModuleHeartBeat NOOP = new ModuleHeartBeat() {
        public ModuleIdentity identity() {
            return ModuleIdentity.of("NOOP");
        }
        public void reportHealthy() {}
        public void reportUnhealthy() {}
        public void start() {}
        public void stop() {}
    };
}
