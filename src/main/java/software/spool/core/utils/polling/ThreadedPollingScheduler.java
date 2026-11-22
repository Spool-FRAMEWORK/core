package software.spool.core.utils.polling;

import software.spool.core.model.schedule.Schedule;

import java.time.Clock;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Optional;

/**
 * Runs each round on its own thread when its {@link Schedule} says, and cancels the token when the schedule has no
 * more executions. It waits in steps no longer than {@code longestWait}, so a cancelled token stops it soon.
 */
public class ThreadedPollingScheduler implements PollingScheduler {
    private static final Duration DEFAULT_LONGEST_WAIT = Duration.ofMillis(100);

    private final Clock clock;
    private final Duration longestWait;

    public ThreadedPollingScheduler() {
        this(Clock.systemUTC(), DEFAULT_LONGEST_WAIT);
    }

    public ThreadedPollingScheduler(Clock clock, Duration longestWait) {
        this.clock = clock;
        this.longestWait = longestWait;
    }

    @Override
    public void schedule(Runnable task, Schedule schedule, CancellationToken token) {
        Thread thread = new Thread(() -> runRounds(task, schedule, token), "spool-polling-thread");
        thread.setDaemon(false);
        thread.start();
    }

    private void runRounds(Runnable task, Schedule schedule, CancellationToken token) {
        Optional<ZonedDateTime> next = Optional.of(schedule.firstExecution(now()));
        while (next.isPresent()) {
            if (!waitUntil(next.get(), token)) return;
            task.run();
            next = schedule.nextExecutionAfter(now());
        }
        token.cancel();
    }

    private boolean waitUntil(ZonedDateTime execution, CancellationToken token) {
        while (token.isActive()) {
            Duration remaining = Duration.between(clock.instant(), execution.toInstant());
            if (!remaining.isPositive()) return true;
            try {
                Thread.sleep(shorterOf(remaining, longestWait));
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return false;
    }

    private ZonedDateTime now() {
        return ZonedDateTime.now(clock);
    }

    private static Duration shorterOf(Duration one, Duration other) {
        return one.compareTo(other) <= 0 ? one : other;
    }
}
