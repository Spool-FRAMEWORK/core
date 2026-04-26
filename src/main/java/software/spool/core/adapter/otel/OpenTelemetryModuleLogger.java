package software.spool.core.adapter.otel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.spool.core.model.watchdog.ModuleIdentity;
import software.spool.core.port.watchdog.ModuleLogger;

import java.time.Duration;

public class OpenTelemetryModuleLogger implements ModuleLogger {
    private static final Logger log = LoggerFactory.getLogger(OpenTelemetryModuleLogger.class);

    public OpenTelemetryModuleLogger() {
        OTELConfig.init("spool");
    }

    @Override
    public void moduleStarted(ModuleIdentity identity) {
        log.info("[{}] Module started", identity.moduleId());
    }

    @Override
    public void moduleStopped(ModuleIdentity identity, String reason) {
        log.error("[{}] Module stopped - reason: {}", identity.moduleId(), reason);
    }

    @Override
    public void moduleDegraded(ModuleIdentity identity, String reason) {
        log.error("[{}] Module degraded — reason: {}", identity.moduleId(), reason);
    }

    @Override
    public void moduleRecovered(ModuleIdentity identity, Duration downtime) {
        log.warn("[{}] Module recovered <UNK> downtime: {}", identity.moduleId(), downtime);
    }
}