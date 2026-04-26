package software.spool.core.model.spool;

import software.spool.core.port.health.HealthProvider;

public interface SpoolModule extends HealthProvider {
    void start(SpoolNode.StartPermit permit);
    void stop(SpoolNode.StartPermit permit);
}