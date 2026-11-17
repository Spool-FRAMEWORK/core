package software.spool.core.adapter.http;

import software.spool.core.port.authentication.Authentication;
import software.spool.core.port.http.Caller;
import software.spool.core.port.http.HttpRequest;
import software.spool.core.port.http.HttpResponse;
import software.spool.core.port.http.HttpRoute;
import software.spool.core.port.http.RouteAccess;
import software.spool.core.port.logging.Logger;

/**
 * Lets a request reach its route only when the route's provider identifies the caller and the caller holds the
 * route's role (ADR-013). A request nobody could identify gets {@code 401}, and one without the role gets
 * {@code 403}. Neither response says why: the reason goes to the log, as the provider gave it, without credentials.
 */
final class RouteAccessGuard {
    private static final HttpResponse UNAUTHENTICATED = HttpResponse.json(401, "{\"error\":\"unauthenticated\"}");
    private static final HttpResponse FORBIDDEN = HttpResponse.json(403, "{\"error\":\"forbidden\"}");

    private final Logger logger;

    RouteAccessGuard(Logger logger) {
        this.logger = logger;
    }

    HttpResponse handle(HttpRoute route, HttpRequest request) {
        return switch (route.access()) {
            case RouteAccess.Open open -> route.handler().handle(request, Caller.anonymous());
            case RouteAccess.Protected guarded -> switch (guarded.authenticator().authenticate(request)) {
                case Authentication.Rejected rejected -> {
                    logger.warn("Rejected {} {}: {}", request.method(), request.path(), rejected.reason());
                    yield UNAUTHENTICATED;
                }
                case Authentication.Accepted accepted when !accepted.identity().hasRole(guarded.role()) -> {
                    logger.warn("Refused {} {} to {} from {}: it lacks the role {}", request.method(), request.path(),
                            accepted.identity().subject(), accepted.identity().provider(), guarded.role());
                    yield FORBIDDEN;
                }
                case Authentication.Accepted accepted ->
                        route.handler().handle(request, new Caller.Identified(accepted.identity()));
            };
        };
    }
}
