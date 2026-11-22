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

    /**
     * Tells the heartbeat of the module where to read its health, when something wraps the module and adds to its
     * health, such as the pieces it uses. A module without a heartbeat ignores it.
     */
    default void reportHealthFrom(HealthProvider health) {
    }
}