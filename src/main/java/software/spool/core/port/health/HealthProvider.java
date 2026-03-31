package software.spool.core.port.health;

public interface HealthProvider {
    HealthPayload checkHealth();
}