package software.spool.core.utils.polling;

public interface PollingScheduler {
    void schedule(Runnable task, PollingPolicy policy, CancellationToken token);
}
