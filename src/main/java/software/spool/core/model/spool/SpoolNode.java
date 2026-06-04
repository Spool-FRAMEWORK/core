package software.spool.core.model.spool;

import software.spool.core.adapter.health.HTTPHealthServer;
import software.spool.core.port.health.HealthServer;
import software.spool.core.port.health.HealthStatus;
import software.spool.core.port.health.ModuleHealthPayload;
import software.spool.core.port.health.NodeHealthPayload;
import software.spool.core.port.metrics.MetricsRegistry;
import software.spool.core.port.metrics.SpoolMetrics;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SpoolNode {
    private final List<SpoolModule> modules = new ArrayList<>();
    private final Map<SpoolModule, String> moduleIds = new HashMap<>();
    private final Map<String, HealthStatus> lastKnownStatus = new ConcurrentHashMap<>();
    private final HealthServer healthServer;
    private final String nodeId;
    private final MetricsRegistry.CounterMetric moduleStarted;
    private final MetricsRegistry.CounterMetric moduleStopped;
    private final MetricsRegistry.CounterMetric moduleDegraded;

    public static final class StartPermit {
        private StartPermit() {}
    }

    private static final StartPermit PERMIT = new StartPermit();

    private SpoolNode(int healthPort, String nodeId, MetricsRegistry metricsRegistry) {
        this.healthServer = new HTTPHealthServer(healthPort, this::aggregateHealth);
        this.nodeId = nodeId;
        this.moduleStarted = metricsRegistry.counter(SpoolMetrics.Module.STARTED_TOTAL, SpoolMetrics.Module.STARTED_TOTAL_DESC, "events");
        this.moduleStopped = metricsRegistry.counter(SpoolMetrics.Module.STOPPED_TOTAL, SpoolMetrics.Module.STOPPED_TOTAL_DESC, "events");
        this.moduleDegraded = metricsRegistry.counter(SpoolMetrics.Module.DEGRADED_TOTAL, SpoolMetrics.Module.DEGRADED_TOTAL_DESC, "events");
    }

    public static SpoolNode create() {
        return new SpoolNode(8080, "spool-node-" + UUID.randomUUID().toString().substring(0, 8), MetricsRegistry.NOOP);
    }

    public static SpoolNode create(int port) {
        return new SpoolNode(port, "spool-node-" + UUID.randomUUID().toString().substring(0, 8), MetricsRegistry.NOOP);
    }

    public static SpoolNode create(MetricsRegistry metricsRegistry) {
        return new SpoolNode(8080, "spool-node-" + UUID.randomUUID().toString().substring(0, 8), metricsRegistry);
    }

    public static SpoolNode create(int port, MetricsRegistry metricsRegistry) {
        return new SpoolNode(port, "spool-node-" + UUID.randomUUID().toString().substring(0, 8), metricsRegistry);
    }

    public SpoolNode register(SpoolModule module) {
        modules.add(module);
        moduleIds.put(module, module.checkHealth().moduleId());
        return this;
    }

    public void start() throws IOException {
        healthServer.start();
        modules.forEach(m -> {
            m.start(PERMIT);
            moduleStarted.increment(Map.of(SpoolMetrics.Attributes.MODULE, moduleIds.get(m)));
        });
    }

    public void stop() {
        modules.forEach(m -> {
            m.stop(PERMIT);
            moduleStopped.increment(Map.of(SpoolMetrics.Attributes.MODULE, moduleIds.get(m)));
        });
        healthServer.stop();
    }

    public SpoolNode and(SpoolNode other) {
        other.modules.forEach(this::register);
        return this;
    }

    private NodeHealthPayload aggregateHealth() {
        List<ModuleHealthPayload> payloads = modules.stream()
                .map(SpoolModule::checkHealth)
                .toList();

        payloads.forEach(payload -> {
            HealthStatus previous = lastKnownStatus.put(payload.moduleId(), payload.status());
            if (previous == HealthStatus.HEALTHY && payload.status() != HealthStatus.HEALTHY) {
                moduleDegraded.increment(Map.of(SpoolMetrics.Attributes.MODULE, payload.moduleId()));
            }
        });

        return NodeHealthPayload.of(nodeId, payloads);
    }
}