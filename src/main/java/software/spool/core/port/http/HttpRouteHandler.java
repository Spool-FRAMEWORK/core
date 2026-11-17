package software.spool.core.port.http;

/** Answers the requests of one route, once the node has checked who may call it. */
@FunctionalInterface
public interface HttpRouteHandler {
    HttpResponse handle(HttpRequest request, Caller caller);
}
