package software.spool.core.port.http;

import java.io.IOException;

/** The single HTTP server of a node, where its health and the routes of its modules are answered. */
public interface NodeHttpServer {
    /**
     * @throws IllegalArgumentException if the method and path already have a route
     */
    void register(HttpRoute route);

    /** Takes the route out, so its method and path find nothing. A route the server does not have changes nothing. */
    void unregister(HttpRoute route);

    void start() throws IOException;

    void stop();
}
