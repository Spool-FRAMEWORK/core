package software.spool.core.port.health;

import java.time.Instant;
import java.util.List;

/**
 * The health of a module: its own checks and the pieces of infrastructure it uses. Its status is the worst of them.
 */
public record ModuleHealthPayload(
        String moduleId,
        HealthStatus status,
        List<HealthCheck> checks,
        Instant timestamp,
        List<PieceHealth> pieces
) {
    public static ModuleHealthPayload healthy(String moduleId) {
        return new ModuleHealthPayload(moduleId, HealthStatus.HEALTHY, List.of(), Instant.now(), List.of());
    }

    public static ModuleHealthPayload degraded(String moduleId, List<HealthCheck> checks) {
        return new ModuleHealthPayload(moduleId, HealthStatus.DEGRADED, checks, Instant.now(), List.of());
    }

    public static ModuleHealthPayload unhealthy(String moduleId, List<HealthCheck> checks) {
        return new ModuleHealthPayload(moduleId, HealthStatus.UNHEALTHY, checks, Instant.now(), List.of());
    }

    public static ModuleHealthPayload of(String moduleId, List<HealthCheck> checks) {
        HealthStatus worst = checks.stream()
                .map(HealthCheck::status)
                .reduce(HealthStatus.HEALTHY, ModuleHealthPayload::worst);
        return new ModuleHealthPayload(moduleId, worst, checks, Instant.now(), List.of());
    }

    /**
     * The same health with the pieces the module uses, and the worst status of the module and its pieces.
     */
    public ModuleHealthPayload withPieces(List<PieceHealth> pieces) {
        HealthStatus worst = pieces.stream()
                .map(PieceHealth::status)
                .reduce(status, ModuleHealthPayload::worst);
        return new ModuleHealthPayload(moduleId, worst, checks, timestamp, List.copyOf(pieces));
    }

    private static HealthStatus worst(HealthStatus a, HealthStatus b) {
        if (a == HealthStatus.UNHEALTHY || b == HealthStatus.UNHEALTHY) return HealthStatus.UNHEALTHY;
        if (a == HealthStatus.DEGRADED  || b == HealthStatus.DEGRADED)  return HealthStatus.DEGRADED;
        return HealthStatus.HEALTHY;
    }
}
