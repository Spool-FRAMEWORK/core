package software.spool.core.port.health;

public record HealthCheck(String name, HealthStatus status, String reason) {

    public static HealthCheck of(String name, HealthStatus status) {
        return new HealthCheck(name, status, null);
    }

    public static HealthCheck healthy(String name) {
        return new HealthCheck(name, HealthStatus.HEALTHY, null);
    }

    public static HealthCheck degraded(String name, String reason) {
        return new HealthCheck(name, HealthStatus.DEGRADED, reason);
    }

    public static HealthCheck unhealthy(String name, String reason) {
        return new HealthCheck(name, HealthStatus.UNHEALTHY, reason);
    }
}