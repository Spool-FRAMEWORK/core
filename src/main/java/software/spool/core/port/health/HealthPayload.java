package software.spool.core.port.health;

import java.time.Instant;
import java.util.List;

public record HealthPayload(
        HealthStatus status,
        String moduleId,
        List<HealthCheck> checks,
        Instant timestamp
) {
    public static HealthPayload healthy(String moduleId) {
        return new HealthPayload(HealthStatus.HEALTHY, moduleId, List.of(), Instant.now());
    }

    public static HealthPayload degraded(String moduleId, List<HealthCheck> checks) {
        return new HealthPayload(HealthStatus.DEGRADED, moduleId, checks, Instant.now());
    }

    public static HealthPayload unhealthy(String moduleId, List<HealthCheck> checks) {
        return new HealthPayload(HealthStatus.UNHEALTHY, moduleId, checks, Instant.now());
    }
}
