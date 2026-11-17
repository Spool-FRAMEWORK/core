package software.spool.core.port.http;

import java.io.IOException;

/** The single HTTP server of a node, where its health and the routes of its modules are answered. */
public interface NodeHttpServer {
    /**
     * @throws IllegalArgumentException if the method and path already have a route
     */
    void register(HttpRoute route);

    void start() throws IOException;

    void stop();
}
