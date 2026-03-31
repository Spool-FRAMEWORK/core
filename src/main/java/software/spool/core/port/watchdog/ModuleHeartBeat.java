package software.spool.core.port.watchdog;

public interface ModuleHeartBeat {
    void reportHealthy();
    void reportUnhealthy();
    void start();
    void stop();

    ModuleHeartBeat NOOP = new ModuleHeartBeat() {
        public void reportHealthy() {}
        public void reportUnhealthy() {}
        public void start() {}
        public void stop() {}
    };
}
