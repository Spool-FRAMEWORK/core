package software.spool.core.port;

import software.spool.core.utils.CancellationToken;
import software.spool.core.utils.PollingPolicy;

public interface PollingScheduler {
    void schedule(Runnable task, PollingPolicy policy, CancellationToken token);
}
