package software.spool.core.port.http;

import java.io.IOException;

/** The single HTTP server of a node, where its health and the routes of its modules are answered. */
public interface NodeHttpServer {
    /**
     * @throws IllegalArgumentException if the method and path already have a route
     */
    void register(HttpRoute route);

    /**
     * Takes out the route of the method and path of {@code route}, so they find nothing. A route the server does not
     * have changes nothing. Registering and taking out are safe to call while the server answers requests.
     */
    void unregister(HttpRoute route);

    void start() throws IOException;

    void stop();
}
