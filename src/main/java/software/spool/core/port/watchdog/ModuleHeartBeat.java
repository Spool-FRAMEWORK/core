package software.spool.core.port.watchdog;

import software.spool.core.model.watchdog.ModuleIdentity;
import software.spool.core.port.health.HealthProvider;

/**
 * The beat a module sends to the watchdog, with the health it reads from the provider it was given.
 */
public interface ModuleHeartBeat {
    ModuleIdentity identity();

    /** Where each beat reads the health of the module, such as the module itself or a module that adds its pieces. */
    void reportFrom(HealthProvider health);

    void start();

    void stop();

    ModuleHeartBeat NOOP = new ModuleHeartBeat() {
        public ModuleIdentity identity() {
            return ModuleIdentity.of("NOOP");
        }
        public void reportFrom(HealthProvider health) {}
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
            public void reportFrom(HealthProvider health) {}
            public void start() {}
            public void stop() {}
        };
    }
}
