package software.spool.core.adapter.http;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.spool.core.port.http.HttpMethod;
import software.spool.core.port.http.HttpResponse;
import software.spool.core.port.http.HttpRoute;
import software.spool.core.port.http.RouteAccess;
import software.spool.core.port.logging.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;

import static org.assertj.core.api.Assertions.assertThat;

class JdkNodeHttpServerTest {

    private final HttpClient client = HttpClient.newHttpClient();
    private JdkNodeHttpServer server;
    private int port;

    @BeforeEach
    void startServer() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            port = socket.getLocalPort();
        }
        server = new JdkNodeHttpServer(port, Logger.NOOP);
        server.register(new HttpRoute(HttpMethod.POST, "/spool/echo", RouteAccess.open(),
                (request, caller) -> HttpResponse.json(200, new String(request.body()))));
        server.register(new HttpRoute(HttpMethod.GET, "/spool/broken", RouteAccess.open(),
                (request, caller) -> { throw new IllegalStateException("database password is hunter2"); }));
        server.start();
    }

    @AfterEach
    void stopServer() {
        server.stop();
    }

    @Test
    void request_toARegisteredRoute_getsWhatTheHandlerAnswers() throws Exception {
        var response = send(HttpRequest.newBuilder(uri("/spool/echo")).POST(HttpRequest.BodyPublishers.ofString("{\"a\":1}")));

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).isEqualTo("{\"a\":1}");
        assertThat(response.headers().firstValue("Content-Type")).contains("application/json");
    }

    @Test
    void request_toAnUnknownPath_isNotFound() throws Exception {
        assertThat(send(HttpRequest.newBuilder(uri("/spool/missing")).GET()).statusCode()).isEqualTo(404);
    }

    @Test
    void request_withAMethodTheRouteDoesNotTake_isNotAllowed() throws Exception {
        assertThat(send(HttpRequest.newBuilder(uri("/spool/echo")).GET()).statusCode()).isEqualTo(405);
    }

    @Test
    void handlerThatFails_answersAServerErrorWithoutItsMessage() throws Exception {
        var response = send(HttpRequest.newBuilder(uri("/spool/broken")).GET());

        assertThat(response.statusCode()).isEqualTo(500);
        assertThat(response.body()).doesNotContain("hunter2");
    }

    private java.net.http.HttpResponse<String> send(HttpRequest.Builder request) throws Exception {
        return client.send(request.build(), java.net.http.HttpResponse.BodyHandlers.ofString());
    }

    private URI uri(String path) {
        return URI.create("http://localhost:" + port + path);
    }
}
