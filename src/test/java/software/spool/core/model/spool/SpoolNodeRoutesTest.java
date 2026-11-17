package software.spool.core.model.spool;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import software.spool.core.port.health.ModuleHealthPayload;
import software.spool.core.port.http.HttpMethod;
import software.spool.core.port.http.HttpResponse;
import software.spool.core.port.http.HttpRoute;
import software.spool.core.port.http.RouteAccess;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SpoolNodeRoutesTest {

    private SpoolNode node;

    @AfterEach
    void stopNode() {
        if (node != null) node.stop();
    }

    @Test
    void start_registersTheRoutesOfItsModulesNextToItsHealth() throws Exception {
        int port = freePort();
        node = SpoolNode.create(port).register(new ModuleWithARoute());

        node.start();

        HttpClient client = HttpClient.newHttpClient();
        var fromTheModule = client.send(HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/spool/module")).GET().build(),
                java.net.http.HttpResponse.BodyHandlers.ofString());
        var health = client.send(HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/spool/health")).GET().build(),
                java.net.http.HttpResponse.BodyHandlers.ofString());
        assertThat(fromTheModule.statusCode()).isEqualTo(200);
        assertThat(fromTheModule.body()).isEqualTo("{\"from\":\"module\"}");
        assertThat(health.statusCode()).isEqualTo(200);
    }

    private static int freePort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }

    private static final class ModuleWithARoute implements SpoolModule {
        @Override
        public void start(SpoolNode.StartPermit permit) {
        }

        @Override
        public void stop(SpoolNode.StartPermit permit) {
        }

        @Override
        public ModuleHealthPayload checkHealth() {
            return ModuleHealthPayload.healthy("module-with-a-route");
        }

        @Override
        public Collection<HttpRoute> routes() {
            return List.of(new HttpRoute(HttpMethod.GET, "/spool/module", RouteAccess.open(),
                    (request, caller) -> HttpResponse.json(200, "{\"from\":\"module\"}")));
        }
    }
}
