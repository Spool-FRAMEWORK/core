package software.spool.core.adapter.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import software.spool.core.adapter.logging.LoggerFactory;
import software.spool.core.port.http.HttpMethod;
import software.spool.core.port.http.HttpRequest;
import software.spool.core.port.http.HttpResponse;
import software.spool.core.port.http.HttpRoute;
import software.spool.core.port.http.NodeHttpServer;
import software.spool.core.port.logging.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

/**
 * The node's HTTP server, on the JDK's {@code com.sun.net.httpserver}. A request is matched by its exact path and
 * method; a path nobody registered gets {@code 404} and a method the path does not take gets {@code 405}. Every
 * request goes through the access check of its route, and a route that fails answers {@code 500} without its
 * message, which is logged instead.
 */
public final class JdkNodeHttpServer implements NodeHttpServer {
    private static final HttpResponse NOT_FOUND = HttpResponse.json(404, "{\"error\":\"not found\"}");
    private static final HttpResponse METHOD_NOT_ALLOWED = HttpResponse.json(405, "{\"error\":\"method not allowed\"}");
    private static final HttpResponse SERVER_ERROR = HttpResponse.json(500, "{\"error\":\"internal error\"}");

    private final int port;
    private final Logger logger;
    private final RouteAccessGuard guard;
    private final Map<String, Map<HttpMethod, HttpRoute>> routesByPath = new ConcurrentHashMap<>();
    private HttpServer server;

    public JdkNodeHttpServer(int port) {
        this(port, LoggerFactory.getLogger(JdkNodeHttpServer.class));
    }

    public JdkNodeHttpServer(int port, Logger logger) {
        this.port = port;
        this.logger = logger;
        this.guard = new RouteAccessGuard(logger);
    }

    @Override
    public void register(HttpRoute route) {
        Map<HttpMethod, HttpRoute> byMethod = routesByPath.computeIfAbsent(route.path(), path -> new ConcurrentHashMap<>());
        if (byMethod.putIfAbsent(route.method(), route) != null) {
            throw new IllegalArgumentException("The node already has a route for " + route.method() + " " + route.path());
        }
    }

    @Override
    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", new TracedHttpHandler(this::handle));
        server.setExecutor(Executors.newCachedThreadPool(runnable -> {
            Thread thread = new Thread(runnable, "spool-node-http");
            thread.setDaemon(true);
            return thread;
        }));
        server.start();
        logger.info("Node HTTP server started on port {}", port);
    }

    @Override
    public void stop() {
        if (server != null) server.stop(0);
    }

    private void handle(HttpExchange exchange) throws IOException {
        respond(exchange, answer(exchange));
    }

    private HttpResponse answer(HttpExchange exchange) {
        String path = exchange.getRequestURI().getPath();
        Map<HttpMethod, HttpRoute> byMethod = routesByPath.get(path);
        if (byMethod == null) return NOT_FOUND;
        Optional<HttpRoute> route = methodOf(exchange).map(byMethod::get);
        if (route.isEmpty()) return METHOD_NOT_ALLOWED;
        try {
            return guard.handle(route.get(), requestOf(exchange, route.get().method(), path));
        } catch (Exception e) {
            logger.error("Route " + route.get().method() + " " + path + " failed", e);
            return SERVER_ERROR;
        }
    }

    private static Optional<HttpMethod> methodOf(HttpExchange exchange) {
        try {
            return Optional.of(HttpMethod.valueOf(exchange.getRequestMethod().toUpperCase()));
        } catch (IllegalArgumentException unknown) {
            return Optional.empty();
        }
    }

    private static HttpRequest requestOf(HttpExchange exchange, HttpMethod method, String path) throws IOException {
        try (InputStream body = exchange.getRequestBody()) {
            return new HttpRequest(method, path, exchange.getRequestHeaders(), body.readAllBytes());
        }
    }

    private static void respond(HttpExchange exchange, HttpResponse response) throws IOException {
        response.headers().forEach(exchange.getResponseHeaders()::set);
        byte[] body = response.body();
        exchange.sendResponseHeaders(response.status(), body.length == 0 ? -1 : body.length);
        try (OutputStream output = exchange.getResponseBody()) {
            output.write(body);
        }
    }
}
