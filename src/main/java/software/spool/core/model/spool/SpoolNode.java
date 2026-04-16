package software.spool.core.model.spool;

import software.spool.core.adapter.health.HTTPHealthServer;
import software.spool.core.port.health.HealthServer;
import software.spool.core.port.health.NodeHealthPayload;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SpoolNode {
    private final List<SpoolModule> modules = new ArrayList<>();
    private final HealthServer healthServer;
    private final String nodeId;

    public static final class StartPermit {
        private StartPermit() {}
    }

    private static final StartPermit PERMIT = new StartPermit();

    private SpoolNode(int healthPort, String nodeId) {
        this.healthServer = new HTTPHealthServer(healthPort, this::aggregateHealth);
        this.nodeId = nodeId;
    }

    public static SpoolNode create() {
        return new SpoolNode(8080, "spool-node-" + UUID.randomUUID().toString().substring(0, 8));
    }

    public static SpoolNode create(int port) {
        return new SpoolNode(port, "spool-node-" + UUID.randomUUID().toString().substring(0, 8));
    }

    public SpoolNode register(SpoolModule module) {
        modules.add(module);
        return this;
    }

    public void start() throws IOException {
        healthServer.start();
        modules.forEach(m -> m.start(PERMIT));
    }

    private NodeHealthPayload aggregateHealth() {
        return NodeHealthPayload.of(nodeId, modules.stream()
                .map(SpoolModule::checkHealth)
                .toList());
    }
}