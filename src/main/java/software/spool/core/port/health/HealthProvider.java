package software.spool.core.port.health;

public interface HealthProvider {
    ModuleHealthPayload checkHealth();
}