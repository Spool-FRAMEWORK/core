package software.spool.core.model.watchdog;

import software.spool.core.port.health.HealthStatus;

public enum ModuleStatus {
    HEALTHY, DEGRADED, UNHEALTHY;

    public static ModuleStatus of(HealthStatus status) {
        return valueOf(status.name());
    }
}