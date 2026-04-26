package software.spool.core.port.health;

import java.time.Instant;
import java.util.List;

public record ModuleHealthPayload(
        String moduleId,
        HealthStatus status,
        List<HealthCheck> checks,
        Instant timestamp
) {
    public static ModuleHealthPayload healthy(String moduleId) {
        return new ModuleHealthPayload(moduleId, HealthStatus.HEALTHY, List.of(), Instant.now());
    }

    public static ModuleHealthPayload degraded(String moduleId, List<HealthCheck> checks) {
        return new ModuleHealthPayload(moduleId, HealthStatus.DEGRADED, checks, Instant.now());
    }

    public static ModuleHealthPayload unhealthy(String moduleId, List<HealthCheck> checks) {
        return new ModuleHealthPayload(moduleId, HealthStatus.UNHEALTHY, checks, Instant.now());
    }
}