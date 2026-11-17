package software.spool.core.model.spool;

import software.spool.core.port.health.HealthProvider;
import software.spool.core.port.http.HttpRoute;

import java.util.Collection;
import java.util.List;

public interface SpoolModule extends HealthProvider {
    void start(SpoolNode.StartPermit permit);
    void stop(SpoolNode.StartPermit permit);

    /** The HTTP routes the module answers on its node's server. None by default. */
    default Collection<HttpRoute> routes() {
        return List.of();
    }
}