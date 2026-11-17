package software.spool.core.adapter.http;

import software.spool.core.adapter.jackson.RecordSerializerFactory;
import software.spool.core.port.health.NodeHealthPayload;
import software.spool.core.port.http.HttpMethod;
import software.spool.core.port.http.HttpResponse;
import software.spool.core.port.http.HttpRoute;
import software.spool.core.port.http.RouteAccess;

import java.util.List;
import java.util.function.Supplier;

/**
 * The health of the node, open to anyone: the orchestrator's probes call it and it shows no data (ADR-013).
 * The status code follows the health of the node.
 */
public final class HealthRoutes {
    public static final List<String> PATHS = List.of("/spool/health", "/spool/health/live", "/spool/health/ready");

    private HealthRoutes() {
    }

    public static List<HttpRoute> of(Supplier<NodeHealthPayload> health) {
        return PATHS.stream()
                .map(path -> new HttpRoute(HttpMethod.GET, path, RouteAccess.open(), (request, caller) -> {
                    NodeHealthPayload payload = health.get();
                    return HttpResponse.json(payload.status().httpCode(), RecordSerializerFactory.record().serialize(payload))
                            .withHeader("Cache-Control", "no-cache");
                }))
                .toList();
    }
}
