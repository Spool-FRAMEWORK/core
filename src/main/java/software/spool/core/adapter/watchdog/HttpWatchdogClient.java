package software.spool.core.adapter.watchdog;

import software.spool.core.adapter.jackson.PayloadDeserializerFactory;
import software.spool.core.adapter.jackson.RecordSerializerFactory;
import software.spool.core.model.watchdog.HeartbeatPayload;
import software.spool.core.model.watchdog.ModuleIdentity;
import software.spool.core.model.watchdog.ModuleState;
import software.spool.core.model.watchdog.ModuleStatus;
import software.spool.core.port.watchdog.WatchdogClient;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Collection;

public class HttpWatchdogClient implements WatchdogClient {
    private final String baseUrl;
    private final HttpClient http;

    public HttpWatchdogClient(String baseUrl) {
        this.baseUrl = baseUrl;
        this.http = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    @Override
    public void beat(ModuleIdentity identity, ModuleStatus status) {
        String body = RecordSerializerFactory.record()
                .serialize(new HeartbeatPayload(identity, status));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/heartbeat"))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(5))
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        send(request,  HttpResponse.BodyHandlers.ofString());
    }

    @Override
    public Collection<ModuleState> query() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/health"))
                .header("Accept", "application/json")
                .timeout(Duration.ofSeconds(5))
                .GET()
                .build();

        HttpResponse<String> response = send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new RuntimeException("Failed to query health: HTTP " + response.statusCode());
        }
        return PayloadDeserializerFactory.json()
                .asList(ModuleState.class)
                .deserialize(response.body());
    }

    private <T> HttpResponse<T> send(HttpRequest request, HttpResponse.BodyHandler<T> handler) {
        try {
            return http.send(request, handler);
        } catch (Exception e) {
            throw new RuntimeException("Watchdog unreachable at " + baseUrl, e);
        }
    }
}
