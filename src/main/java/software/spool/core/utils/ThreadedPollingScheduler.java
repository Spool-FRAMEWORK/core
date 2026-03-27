package software.spool.core.utils;

import software.spool.core.port.PollingScheduler;

import java.time.Duration;
import java.time.Instant;

public class ThreadedPollingScheduler implements PollingScheduler {
    private static final Duration CHECK_INTERVAL = Duration.ofMillis(100);

    @Override
    public void schedule(Runnable task, PollingPolicy policy, CancellationToken token) {
        Thread t = new Thread(() -> {
            Instant lastRun = Instant.EPOCH;
            while (token.isActive()) {
                if (!policy.shouldPoll(Duration.between(lastRun, Instant.now()))) continue;
                task.run();
                lastRun = Instant.now();
                if (policy.isOneShot() || shouldEndThread()) {
                    token.cancel();
                    break;
                }
            }
        }, "spool-polling-thread");
        t.setDaemon(false);
        t.start();
    }

    private static boolean shouldEndThread() {
        try {
            Thread.sleep(CHECK_INTERVAL.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return true;
        }
        return false;
    }
}
