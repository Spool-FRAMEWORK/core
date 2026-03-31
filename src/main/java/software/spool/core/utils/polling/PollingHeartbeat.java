package software.spool.core.utils.polling;

import software.spool.core.model.watchdog.ModuleIdentity;
import software.spool.core.model.watchdog.ModuleStatus;
import software.spool.core.port.watchdog.ModuleHeartBeat;
import software.spool.core.port.watchdog.WatchdogHeartBeat;

public class PollingHeartbeat implements ModuleHeartBeat {
    private final WatchdogHeartBeat heartbeat;
    private final String moduleId;
    private final PollingConfiguration polling;
    private volatile ModuleStatus currentStatus = ModuleStatus.HEALTHY;
    private CancellationToken token = CancellationToken.NOOP;

    public PollingHeartbeat(WatchdogHeartBeat heartbeat, ModuleIdentity identity) {
        this.heartbeat = heartbeat;
        this.moduleId = identity.moduleId();
        this.polling = PollingConfiguration.every(identity.heartbeatInterval());
    }

    @Override
    public void start() {
        token = CancellationToken.create();
        polling.scheduler().schedule(
            () -> heartbeat.beat(moduleId, currentStatus),
            polling.policy(),
            token
        );
    }

    @Override public void stop() { token.cancel(); }
    @Override public void reportHealthy() { currentStatus = ModuleStatus.HEALTHY; }
    @Override public void reportUnhealthy() { currentStatus = ModuleStatus.UNHEALTHY; }
}