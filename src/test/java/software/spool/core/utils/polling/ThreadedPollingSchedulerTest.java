package software.spool.core.utils.polling;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import software.spool.core.adapter.schedule.CronUtilsCronCalendar;
import software.spool.core.model.schedule.CronExpression;
import software.spool.core.model.schedule.Schedule;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;

import static org.assertj.core.api.Assertions.assertThat;

class ThreadedPollingSchedulerTest {
    private static final Duration WAIT_STEP = Duration.ofMillis(1);
    private static final Duration PATIENCE = Duration.ofSeconds(5);
    private static final Duration QUIET_PERIOD = Duration.ofMillis(200);

    private final MovableClock clock = new MovableClock(Instant.parse("2026-11-30T23:59:00Z"));
    private final ThreadedPollingScheduler scheduler = new ThreadedPollingScheduler(clock, WAIT_STEP);
    private final AtomicInteger rounds = new AtomicInteger();
    private final CancellationToken token = CancellationToken.create();

    @AfterEach
    void stopTheScheduler() {
        token.cancel();
    }

    @Test
    void cronScheduleWaitsForItsDateBeforeTheFirstRound() throws InterruptedException {
        Schedule firstOfTheMonthAtEight = Schedule.cron(
                CronExpression.parse("0 8 1 * *", new CronUtilsCronCalendar()), ZoneId.of("UTC"));

        scheduler.schedule(rounds::incrementAndGet, firstOfTheMonthAtEight, token);
        Thread.sleep(QUIET_PERIOD.toMillis());
        assertThat(rounds).hasValue(0);

        clock.moveTo(Instant.parse("2026-12-01T08:00:00Z"));
        awaitUntil(() -> rounds.get() == 1);
        assertThat(rounds).hasValue(1);
    }

    @Test
    void onceRunsOneRoundAndCancelsTheToken() throws InterruptedException {
        scheduler.schedule(rounds::incrementAndGet, Schedule.once(), token);

        awaitUntil(token::isCancelled);
        assertThat(rounds).hasValue(1);
        assertThat(token.isCancelled()).isTrue();
    }

    @Test
    void everyRunsWhenItStartsAndAgainOnceTheIntervalHasPassed() throws InterruptedException {
        scheduler.schedule(rounds::incrementAndGet, Schedule.every(Duration.ofMinutes(5)), token);
        awaitUntil(() -> rounds.get() == 1);
        Thread.sleep(QUIET_PERIOD.toMillis());
        assertThat(rounds).hasValue(1);

        clock.moveTo(clock.instant().plus(Duration.ofMinutes(5)));
        awaitUntil(() -> rounds.get() == 2);
        assertThat(rounds).hasValue(2);
    }

    @Test
    void cancelledTokenStopsTheRounds() throws InterruptedException {
        scheduler.schedule(rounds::incrementAndGet, Schedule.every(Duration.ofMinutes(5)), token);
        awaitUntil(() -> rounds.get() == 1);

        token.cancel();
        clock.moveTo(clock.instant().plus(Duration.ofMinutes(10)));
        Thread.sleep(QUIET_PERIOD.toMillis());
        assertThat(rounds).hasValue(1);
    }

    private static void awaitUntil(BooleanSupplier condition) throws InterruptedException {
        Instant deadline = Instant.now().plus(PATIENCE);
        while (!condition.getAsBoolean() && Instant.now().isBefore(deadline)) {
            Thread.sleep(WAIT_STEP.toMillis());
        }
    }

    private static final class MovableClock extends Clock {
        private volatile Instant now;

        private MovableClock(Instant now) {
            this.now = now;
        }

        void moveTo(Instant instant) {
            now = instant;
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }
}
