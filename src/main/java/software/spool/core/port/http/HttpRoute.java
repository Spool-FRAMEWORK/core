package software.spool.core.port.http;

import java.util.Objects;

/** A method and path the node answers, who may call it and what answers it. */
public record HttpRoute(HttpMethod method, String path, RouteAccess access, HttpRouteHandler handler) {
    public HttpRoute {
        Objects.requireNonNull(method, "method is required");
        Objects.requireNonNull(access, "access is required");
        Objects.requireNonNull(handler, "handler is required");
        if (path == null || !path.startsWith("/")) throw new IllegalArgumentException("A route path must start with '/', got '" + path + "'");
    }
}
