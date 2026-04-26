package software.spool.core.port.health;

public enum HealthStatus {
    HEALTHY, DEGRADED, UNHEALTHY;

    public int httpCode() {
        return this == UNHEALTHY ? 503 : 200;
    }
}
