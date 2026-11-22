package software.spool.core.adapter.health;

import software.spool.core.model.spool.SpoolModule;
import software.spool.core.model.spool.SpoolNode;
import software.spool.core.port.health.HealthProvider;
import software.spool.core.port.health.ModuleHealthPayload;
import software.spool.core.port.health.PieceMonitor;
import software.spool.core.port.http.HttpRoute;

import java.util.Collection;
import java.util.List;

/**
 * A module whose health also tells how each piece of infrastructure it uses is doing.
 */
public final class MonitoredModule implements SpoolModule {
    private final SpoolModule delegate;
    private final List<PieceMonitor> pieces;

    public MonitoredModule(SpoolModule delegate, List<PieceMonitor> pieces) {
        this.delegate = delegate;
        this.pieces = List.copyOf(pieces);
        delegate.reportHealthFrom(this);
    }

    @Override
    public void reportHealthFrom(HealthProvider health) {
        delegate.reportHealthFrom(health);
    }

    @Override
    public void start(SpoolNode.StartPermit permit) {
        delegate.start(permit);
    }

    @Override
    public void stop(SpoolNode.StartPermit permit) {
        delegate.stop(permit);
    }

    @Override
    public Collection<HttpRoute> routes() {
        return delegate.routes();
    }

    @Override
    public ModuleHealthPayload checkHealth() {
        return delegate.checkHealth().withPieces(pieces.stream().map(PieceMonitor::health).toList());
    }
}
