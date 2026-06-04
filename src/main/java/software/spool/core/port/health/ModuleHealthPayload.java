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

    public static ModuleHealthPayload of(String moduleId, List<HealthCheck> checks) {
        HealthStatus worst = checks.stream()
                .map(HealthCheck::status)
                .reduce(HealthStatus.HEALTHY, ModuleHealthPayload::worst);
        return new ModuleHealthPayload(moduleId, worst, checks, Instant.now());
    }

    private static HealthStatus worst(HealthStatus a, HealthStatus b) {
        if (a == HealthStatus.UNHEALTHY || b == HealthStatus.UNHEALTHY) return HealthStatus.UNHEALTHY;
        if (a == HealthStatus.DEGRADED  || b == HealthStatus.DEGRADED)  return HealthStatus.DEGRADED;
        return HealthStatus.HEALTHY;
    }
}