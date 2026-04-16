package software.spool.core.adapter.health;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import software.spool.core.adapter.jackson.RecordSerializerFactory;
import software.spool.core.port.health.NodeHealthPayload;
import software.spool.core.port.health.HealthServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

public class HTTPHealthServer implements HealthServer {
    private final int port;
    private final Supplier<NodeHealthPayload> payloadSupplier;
    private final HttpHandler handler;
    private HttpServer server;

    public HTTPHealthServer(int port, Supplier<NodeHealthPayload> payloadSupplier) {
        this.port = port;
        this.payloadSupplier = payloadSupplier;
        this.handler = new TracedHttpHandler(this::handle);
    }

    @Override
    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/spool/health", handler);
        server.createContext("/spool/health/live", handler);
        server.createContext("/spool/health/ready", handler);
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
        System.out.println("Health server started on port " + port);
    }

    @Override
    public void stop() {
        if (server != null) server.stop(0);
    }

    private void handle(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            respond(exchange, 405, "{\"error\":\"Method Not Allowed\"}");
            return;
        }
        NodeHealthPayload payload = payloadSupplier.get();
        respond(exchange, payload.status().httpCode(), RecordSerializerFactory.record().serialize(payload));
    }

    private void respond(HttpExchange exchange, int code, String payload) throws IOException {
        byte[] bytes = payload.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.getResponseHeaders().set("Cache-Control", "no-cache");
        exchange.sendResponseHeaders(code, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}