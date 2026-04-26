package software.spool.core.port.health;

import java.time.Instant;
import java.util.List;

public record NodeHealthPayload(
        String nodeId,
        HealthStatus status,
        List<ModuleHealthPayload> modules,
        Instant timestamp
) {
    public static NodeHealthPayload of(String nodeId, List<ModuleHealthPayload> modules) {
        HealthStatus status = modules.stream()
                .map(ModuleHealthPayload::status)
                .reduce(HealthStatus.HEALTHY, NodeHealthPayload::worst);

        return new NodeHealthPayload(nodeId, status, List.copyOf(modules), Instant.now());
    }

    private static HealthStatus worst(HealthStatus a, HealthStatus b) {
        if (a == HealthStatus.UNHEALTHY || b == HealthStatus.UNHEALTHY) return HealthStatus.UNHEALTHY;
        if (a == HealthStatus.DEGRADED || b == HealthStatus.DEGRADED) return HealthStatus.DEGRADED;
        return HealthStatus.HEALTHY;
    }
}