package software.spool.core.port.health;

import java.io.IOException;

public interface HealthServer {
    void start() throws IOException;
    void stop();
}
