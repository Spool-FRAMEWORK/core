package software.spool.core.acceptance.node;

import software.spool.core.model.spool.SpoolModule;
import software.spool.core.model.spool.SpoolNode;
import software.spool.core.port.health.ModuleHealthPayload;
import software.spool.core.port.http.HttpRoute;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/** A module that beats every few milliseconds while it runs, and counts its starts and its beats. */
final class BeatingModule implements SpoolModule {
    private static final long BEAT_MILLIS = 20;

    private final String id;
    private final List<HttpRoute> routes;
    private final AtomicInteger starts = new AtomicInteger();
    private final AtomicInteger beats = new AtomicInteger();
    private Optional<ScheduledExecutorService> heartbeat = Optional.empty();

    BeatingModule(String id, List<HttpRoute> routes) {
        this.id = id;
        this.routes = List.copyOf(routes);
    }

    @Override
    public synchronized void start(SpoolNode.StartPermit permit) {
        starts.incrementAndGet();
        ScheduledExecutorService beating = Executors.newSingleThreadScheduledExecutor();
        beating.scheduleAtFixedRate(beats::incrementAndGet, 0, BEAT_MILLIS, TimeUnit.MILLISECONDS);
        heartbeat = Optional.of(beating);
    }

    @Override
    public synchronized void stop(SpoolNode.StartPermit permit) {
        heartbeat.ifPresent(ScheduledExecutorService::shutdownNow);
        heartbeat = Optional.empty();
    }

    @Override
    public Collection<HttpRoute> routes() {
        return routes;
    }

    @Override
    public ModuleHealthPayload checkHealth() {
        return ModuleHealthPayload.healthy(id);
    }

    String id() {
        return id;
    }

    synchronized boolean running() {
        return heartbeat.isPresent();
    }

    int starts() {
        return starts.get();
    }

    int beats() {
        return beats.get();
    }
}
