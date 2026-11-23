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
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Comparator;
import java.util.ArrayList;
import java.nio.charset.StandardCharsets;
import java.net.URLDecoder;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

/**
 * The node's HTTP server, on the JDK's {@code com.sun.net.httpserver}. A request is matched by the shape of its path
 * and its method: a route path may have variable parts, written {@code {name}}, and when several routes match, the
 * one with more literal parts wins. A path nobody registered gets {@code 404} and a method the path does not take
 * gets {@code 405}. Every
 * request goes through the access check of its route, and a route that fails answers {@code 500} without its
 * message, which is logged instead.
 */
public final class JdkNodeHttpServer implements NodeHttpServer {
    private static final String QUERY_PAIR_SEPARATOR = "&";
    private static final String QUERY_VALUE_SEPARATOR = "=";
    private static final HttpResponse NOT_FOUND = HttpResponse.json(404, "{\"error\":\"not found\"}");
    private static final HttpResponse METHOD_NOT_ALLOWED = HttpResponse.json(405, "{\"error\":\"method not allowed\"}");
    private static final HttpResponse SERVER_ERROR = HttpResponse.json(500, "{\"error\":\"internal error\"}");
    private static final HttpResponse TOO_LARGE = HttpResponse.json(413, "{\"error\":\"the body is too large\"}");
    private static final String CONTENT_LENGTH = "Content-Length";

    private final int port;
    private final Logger logger;
    private final RouteAccessGuard guard;
    private final Map<String, Map<HttpMethod, HttpRoute>> routesByPath = new ConcurrentHashMap<>();
    private final Map<String, RoutePattern> patternsByPath = new ConcurrentHashMap<>();
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
        patternsByPath.computeIfAbsent(route.path(), RoutePattern::new);
        Map<HttpMethod, HttpRoute> byMethod = routesByPath.computeIfAbsent(route.path(), path -> new ConcurrentHashMap<>());
        if (byMethod.putIfAbsent(route.method(), route) != null) {
            throw new IllegalArgumentException("The node already has a route for " + route.method() + " " + route.path());
        }
    }

    @Override
    public void unregister(HttpRoute route) {
        routesByPath.computeIfPresent(route.path(), (path, byMethod) -> {
            byMethod.remove(route.method(), route);
            return byMethod.isEmpty() ? null : byMethod;
        });
        if (!routesByPath.containsKey(route.path())) patternsByPath.remove(route.path());
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
        List<String> requestParts = RoutePattern.partsOf(exchange.getRequestURI().getRawPath()).stream()
                .map(part -> URLDecoder.decode(part, StandardCharsets.UTF_8))
                .toList();
        Optional<Matched> matched = patternsByPath.values().stream()
                .flatMap(pattern -> pattern.match(requestParts).map(values -> new Matched(pattern, values)).stream())
                .max(Comparator.comparingLong(candidate -> candidate.pattern().literalParts()));
        if (matched.isEmpty()) return NOT_FOUND;
        Map<HttpMethod, HttpRoute> byMethod = routesByPath.get(matched.get().pattern().path());
        Optional<HttpRoute> route = methodOf(exchange).map(byMethod::get);
        if (route.isEmpty()) return METHOD_NOT_ALLOWED;
        try {
            Optional<byte[]> body = bodyOf(exchange, route.get().maxBodyBytes());
            if (body.isEmpty()) return TOO_LARGE;
            return guard.handle(route.get(), requestOf(exchange, route.get().method(), path, body.get())
                    .withPathParameters(matched.get().values()));
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

    private static HttpRequest requestOf(HttpExchange exchange, HttpMethod method, String path, byte[] body) {
        return new HttpRequest(method, path, exchange.getRequestHeaders(), body, Map.of(),
                queryParametersOf(exchange.getRequestURI().getRawQuery()));
    }

    /**
     * The body of the request, or nothing when it is bigger than the route allows: a declared length over the limit
     * is refused without reading, and an undeclared one is read only up to one byte past the limit.
     */
    private static Optional<byte[]> bodyOf(HttpExchange exchange, Optional<Integer> maxBodyBytes) throws IOException {
        try (InputStream body = exchange.getRequestBody()) {
            if (maxBodyBytes.isEmpty()) return Optional.of(body.readAllBytes());
            int limit = maxBodyBytes.get();
            Optional<Long> declared = Optional.ofNullable(exchange.getRequestHeaders().getFirst(CONTENT_LENGTH))
                    .flatMap(JdkNodeHttpServer::lengthOf);
            if (declared.isPresent() && declared.get() > limit) return Optional.empty();
            byte[] read = body.readNBytes(limit + 1);
            return read.length > limit ? Optional.empty() : Optional.of(read);
        }
    }

    private static Optional<Long> lengthOf(String header) {
        try {
            return Optional.of(Long.parseLong(header.strip()));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    private static Map<String, List<String>> queryParametersOf(String rawQuery) {
        if (rawQuery == null || rawQuery.isEmpty()) return Map.of();
        Map<String, List<String>> parameters = new LinkedHashMap<>();
        for (String pair : rawQuery.split(QUERY_PAIR_SEPARATOR)) {
            if (pair.isEmpty()) continue;
            String[] nameAndValue = pair.split(QUERY_VALUE_SEPARATOR, 2);
            String value = nameAndValue.length > 1 ? nameAndValue[1] : "";
            parameters.computeIfAbsent(URLDecoder.decode(nameAndValue[0], StandardCharsets.UTF_8), name -> new ArrayList<>())
                    .add(URLDecoder.decode(value, StandardCharsets.UTF_8));
        }
        return parameters;
    }

    private record Matched(RoutePattern pattern, Map<String, String> values) {
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
