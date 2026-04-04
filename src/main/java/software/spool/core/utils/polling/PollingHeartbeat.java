package software.spool.core.utils.polling;

import software.spool.core.model.watchdog.ModuleIdentity;
import software.spool.core.model.watchdog.ModuleStatus;
import software.spool.core.port.watchdog.ModuleHeartBeat;
import software.spool.core.port.watchdog.WatchdogHeartBeat;

public class PollingHeartbeat implements ModuleHeartBeat {
    private final WatchdogHeartBeat heartbeat;
    private final ModuleIdentity identity;
    private final PollingConfiguration polling;
    private volatile ModuleStatus currentStatus = ModuleStatus.HEALTHY;
    private CancellationToken token = CancellationToken.NOOP;

    public PollingHeartbeat(WatchdogHeartBeat heartbeat, ModuleIdentity identity) {
        this.heartbeat = heartbeat;
        this.identity = identity;
        this.polling = PollingConfiguration.defaults();
    }

    @Override
    public void start() {
        token = CancellationToken.create();
        polling.scheduler().schedule(
            () -> {
                try {
                    heartbeat.beat(identity, currentStatus);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            },
            polling.policy(),
            token
        );
    }

    @Override public void stop() { token.cancel(); }

    @Override
    public ModuleIdentity identity() {
        return identity;
    }

    @Override public void reportHealthy() { currentStatus = ModuleStatus.HEALTHY; }
    @Override public void reportUnhealthy() { currentStatus = ModuleStatus.UNHEALTHY; }
}