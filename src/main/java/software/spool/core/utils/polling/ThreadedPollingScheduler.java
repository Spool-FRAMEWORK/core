package software.spool.core.utils.polling;

import java.time.Duration;
import java.time.Instant;

public class ThreadedPollingScheduler implements PollingScheduler {
    private static final Duration IDLE_SLEEP = Duration.ofMillis(100);

    @Override
    public void schedule(Runnable task, PollingPolicy policy, CancellationToken token) {
        Thread t = new Thread(() -> {
            Instant lastRun = Instant.EPOCH;
            while (token.isActive()) {
                if (!policy.shouldPoll(Duration.between(lastRun, Instant.now()))) {
                    sleep();
                    continue;
                }
                task.run();
                lastRun = Instant.now();
                if (policy.isOneShot()) {
                    token.cancel();
                    break;
                }
            }
        }, "spool-polling-thread");
        t.setDaemon(false);
        t.start();
    }

    private static void sleep() {
        try {
            Thread.sleep(IDLE_SLEEP.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
