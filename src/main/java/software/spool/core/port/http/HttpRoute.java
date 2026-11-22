package software.spool.core.port.http;

import java.util.Objects;
import java.util.Optional;

/**
 * A method and path the node answers, who may call it and what answers it. A route that takes bodies from outside
 * can limit their size: a bigger body is refused before it is read whole.
 */
public record HttpRoute(HttpMethod method, String path, RouteAccess access, HttpRouteHandler handler,
                        Optional<Integer> maxBodyBytes) {
    public HttpRoute {
        Objects.requireNonNull(method, "method is required");
        Objects.requireNonNull(access, "access is required");
        Objects.requireNonNull(handler, "handler is required");
        Objects.requireNonNull(maxBodyBytes, "maxBodyBytes is required");
        if (path == null || !path.startsWith("/")) throw new IllegalArgumentException("A route path must start with '/', got '" + path + "'");
        maxBodyBytes.filter(limit -> limit < 0).ifPresent(limit -> {
            throw new IllegalArgumentException("A body limit cannot be negative, got " + limit);
        });
    }

    public HttpRoute(HttpMethod method, String path, RouteAccess access, HttpRouteHandler handler) {
        this(method, path, access, handler, Optional.empty());
    }

    /** The same route, refusing bodies bigger than {@code bytes}. */
    public HttpRoute withMaxBodyBytes(int bytes) {
        return new HttpRoute(method, path, access, handler, Optional.of(bytes));
    }
}
