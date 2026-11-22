package software.spool.core.utils.polling;

import software.spool.core.adapter.logging.LoggerFactory;
import software.spool.core.model.watchdog.ModuleIdentity;
import software.spool.core.port.health.HealthProvider;
import software.spool.core.port.health.ModuleHealthPayload;
import software.spool.core.port.logging.Logger;
import software.spool.core.port.watchdog.ModuleHeartBeat;
import software.spool.core.port.watchdog.WatchdogHeartBeat;

/**
 * Sends the health of a module to the watchdog on a schedule. Until it is told where to read that health, it reports
 * the module as healthy.
 */
public class PollingHeartbeat implements ModuleHeartBeat {
    private static final Logger LOG = LoggerFactory.getLogger(PollingHeartbeat.class);

    private final WatchdogHeartBeat heartbeat;
    private final ModuleIdentity identity;
    private final PollingConfiguration polling;
    private volatile HealthProvider health;
    private CancellationToken token = CancellationToken.NOOP;

    public PollingHeartbeat(WatchdogHeartBeat heartbeat, ModuleIdentity identity) {
        this(heartbeat, identity, PollingConfiguration.defaults());
    }

    public PollingHeartbeat(WatchdogHeartBeat heartbeat, ModuleIdentity identity, PollingConfiguration polling) {
        this.heartbeat = heartbeat;
        this.identity = identity;
        this.polling = polling;
        this.health = () -> ModuleHealthPayload.healthy(identity.moduleId());
    }

    @Override
    public void reportFrom(HealthProvider health) {
        this.health = health;
    }

    @Override
    public void start() {
        token = CancellationToken.create();
        polling.scheduler().schedule(this::beat, polling.schedule(), token);
    }

    @Override
    public void stop() {
        token.cancel();
    }

    @Override
    public ModuleIdentity identity() {
        return identity;
    }

    private void beat() {
        try {
            heartbeat.beat(identity, health.checkHealth());
        } catch (Exception e) {
            LOG.warn("The heartbeat of the module {} was not sent: {}", identity.moduleId(), e.getMessage());
        }
    }
}
