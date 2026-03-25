package software.spool.core.utils;

import software.spool.core.port.PollingScheduler;

import java.time.Duration;
import java.time.Instant;

public class ThreadedPollingScheduler implements PollingScheduler {
    private static final Duration CHECK_INTERVAL = Duration.ofSeconds(1);

    @Override
    public void schedule(Runnable task, PollingPolicy policy, CancellationToken token) {
        Thread t = new Thread(() -> {
            Instant lastRun = Instant.EPOCH;
            while (token.isActive()) {
                Duration elapsed = Duration.between(lastRun, Instant.now());
                if (policy.shouldPoll(elapsed)) {
                    task.run();
                    lastRun = Instant.now();
                }
                try {
                    Thread.sleep(CHECK_INTERVAL.toMillis());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }, "spool-feeder-polling");
        t.setDaemon(false);
        t.start();
    }
}
