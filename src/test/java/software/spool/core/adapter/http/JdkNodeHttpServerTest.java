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
        server.register(new HttpRoute(HttpMethod.GET, "/spool/records/{key}", RouteAccess.open(),
                (request, caller) -> HttpResponse.json(200, "{\"key\":\"" + request.pathParameter("key").orElse("") + "\",\"status\":\""
                        + request.queryParameter("status").orElse("none") + "\"}")));
        server.register(new HttpRoute(HttpMethod.POST, "/spool/records/{key}/purge", RouteAccess.open(),
                (request, caller) -> HttpResponse.json(200, "{\"purged\":\"" + request.pathParameter("key").orElse("") + "\"}")));
        server.register(new HttpRoute(HttpMethod.POST, "/spool/small", RouteAccess.open(),
                (request, caller) -> HttpResponse.json(200, "{\"bytes\":" + request.body().length + "}")).withMaxBodyBytes(8));
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
    void request_toARouteWithAVariablePart_givesTheHandlerThatPartDecoded() throws Exception {
        var response = send(HttpRequest.newBuilder(uri("/spool/records/order%201")).GET());

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).isEqualTo("{\"key\":\"order 1\",\"status\":\"none\"}");
    }

    @Test
    void request_withParametersInTheUrl_givesTheHandlerTheirValuesDecoded() throws Exception {
        var response = send(HttpRequest.newBuilder(uri("/spool/records/order-1?status=PENDING&cursor=a%2Bb")).GET());

        assertThat(response.body()).isEqualTo("{\"key\":\"order-1\",\"status\":\"PENDING\"}");
    }

    @Test
    void request_toALongerPathThanTheRoute_matchesTheRouteWithThatShape() throws Exception {
        var response = send(HttpRequest.newBuilder(uri("/spool/records/order-1/purge")).POST(HttpRequest.BodyPublishers.noBody()));

        assertThat(response.body()).isEqualTo("{\"purged\":\"order-1\"}");
    }

    @Test
    void request_toAPathWithMorePartsThanAnyRoute_isNotFound() throws Exception {
        assertThat(send(HttpRequest.newBuilder(uri("/spool/records/order-1/purge/now")).POST(HttpRequest.BodyPublishers.noBody()))
                .statusCode()).isEqualTo(404);
    }

    @Test
    void request_withABodyWithinTheLimitOfItsRoute_reachesTheHandler() throws Exception {
        var response = send(HttpRequest.newBuilder(uri("/spool/small")).POST(HttpRequest.BodyPublishers.ofString("12345678")));

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).isEqualTo("{\"bytes\":8}");
    }

    @Test
    void request_withABodyBiggerThanTheLimitOfItsRoute_isRefusedAsTooLarge() throws Exception {
        var response = send(HttpRequest.newBuilder(uri("/spool/small")).POST(HttpRequest.BodyPublishers.ofString("123456789")));

        assertThat(response.statusCode()).isEqualTo(413);
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
