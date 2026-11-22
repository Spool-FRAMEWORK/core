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

    /**
     * The heartbeat of a module whose node has no watchdog: it sends nothing, but still knows which module it is.
     */
    static ModuleHeartBeat silent(ModuleIdentity identity) {
        return new ModuleHeartBeat() {
            public ModuleIdentity identity() {
                return identity;
            }
            public void reportHealthy() {}
            public void reportUnhealthy() {}
            public void start() {}
            public void stop() {}
        };
    }
}
