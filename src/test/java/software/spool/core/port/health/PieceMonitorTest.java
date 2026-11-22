package software.spool.core.port.health;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PieceMonitorTest {
    private static final PieceCheckPolicy POLICY = new PieceCheckPolicy(Duration.ofSeconds(10), Duration.ofSeconds(2));

    private final MovingClock clock = new MovingClock(Instant.parse("2026-11-22T10:00:00Z"));

    @Test
    void health_withoutCallsOrCheck_isHealthyWithItsNameAndType() {
        PieceHealth health = monitor(Optional.empty()).health();

        assertThat(health.piece()).isEqualTo("lake");
        assertThat(health.type()).isEqualTo("FILE_SYSTEM");
        assertThat(health.status()).isEqualTo(HealthStatus.HEALTHY);
        assertThat(health.reason()).isEmpty();
        assertThat(health.checkedAt()).isEqualTo(clock.instant());
    }

    @Test
    void health_afterAFailedCall_isUnhealthyWithTheReasonOfTheFailure() {
        PieceMonitor monitor = monitor(Optional.empty());

        assertThatThrownBy(() -> monitor.call(() -> {
            throw new IOException("the folder /lake cannot be written");
        })).isInstanceOf(IOException.class);

        assertThat(monitor.health().status()).isEqualTo(HealthStatus.UNHEALTHY);
        assertThat(monitor.health().reason()).contains("the folder /lake cannot be written");
    }

    @Test
    void health_afterAFailedCallAndThenASuccessfulOne_isHealthyAgain() throws Exception {
        PieceMonitor monitor = monitor(Optional.empty());
        assertThatThrownBy(() -> monitor.run(() -> {
            throw new IllegalStateException("down");
        }));

        String result = monitor.call(() -> "written");

        assertThat(result).isEqualTo("written");
        assertThat(monitor.health().status()).isEqualTo(HealthStatus.HEALTHY);
    }

    @Test
    void health_whenItsCheckFails_isUnhealthyWithTheReasonOfTheCheck() {
        PieceMonitor monitor = monitor(Optional.of(() -> HealthCheck.unhealthy("lake", "the bucket does not exist")));

        assertThat(monitor.health().status()).isEqualTo(HealthStatus.UNHEALTHY);
        assertThat(monitor.health().reason()).contains("the bucket does not exist");
    }

    @Test
    void health_whenItsCheckTakesLongerThanThePolicyAllows_isDegraded() {
        PieceMonitor monitor = monitor(Optional.of(() -> {
            clock.advance(Duration.ofSeconds(3));
            return HealthCheck.healthy("lake");
        }));

        PieceHealth health = monitor.health();

        assertThat(health.status()).isEqualTo(HealthStatus.DEGRADED);
        assertThat(health.reason()).hasValueSatisfying(reason -> assertThat(reason).contains("3000 ms").contains("2000 ms"));
    }

    @Test
    void health_askedAgainWithinTheInterval_doesNotCheckThePieceAgain() {
        AtomicInteger checks = new AtomicInteger();
        PieceMonitor monitor = monitor(Optional.of(() -> {
            checks.incrementAndGet();
            return HealthCheck.healthy("lake");
        }));

        monitor.health();
        clock.advance(Duration.ofSeconds(9));
        monitor.health();
        clock.advance(Duration.ofSeconds(2));
        monitor.health();

        assertThat(checks).hasValue(2);
    }

    @Test
    void health_whenItsCheckPassesButTheLastCallFailed_isUnhealthy() {
        PieceMonitor monitor = monitor(Optional.of(() -> HealthCheck.healthy("lake")));
        assertThatThrownBy(() -> monitor.run(() -> {
            throw new IllegalStateException("write rejected");
        }));

        assertThat(monitor.health().status()).isEqualTo(HealthStatus.UNHEALTHY);
        assertThat(monitor.health().reason()).contains("write rejected");
    }

    @Test
    void health_whenItsCheckThrows_isUnhealthyWithTheReasonOfTheError() {
        PieceMonitor monitor = monitor(Optional.of(() -> {
            throw new IllegalStateException("no route to host");
        }));

        assertThat(monitor.health().status()).isEqualTo(HealthStatus.UNHEALTHY);
        assertThat(monitor.health().reason()).contains("no route to host");
    }

    @Test
    void hasCheck_saysWhetherThePieceHasACheckOfItsOwn() {
        assertThat(monitor(Optional.empty()).hasCheck()).isFalse();
        assertThat(monitor(Optional.of(() -> HealthCheck.healthy("lake"))).hasCheck()).isTrue();
        assertThat(monitor(Optional.empty()).health().hasCheck()).isFalse();
        assertThat(monitor(Optional.of(() -> HealthCheck.healthy("lake"))).health().hasCheck()).isTrue();
    }

    @Test
    void health_afterACallThatEndedInAnAnswerOfThePiece_isHealthy() {
        PieceMonitor monitor = monitor(Optional.empty());

        assertThatThrownBy(() -> monitor.call(() -> {
            throw new IllegalArgumentException("already received");
        }, List.of(IllegalArgumentException.class))).isInstanceOf(IllegalArgumentException.class);

        assertThat(monitor.health().status()).isEqualTo(HealthStatus.HEALTHY);
    }

    @Test
    void stream_whenReadingAnElementFails_marksThePieceUnhealthy() {
        PieceMonitor monitor = monitor(Optional.empty());
        Stream<String> stream = monitor.stream(() -> Stream.of("a", "b").map(value -> {
            if (value.equals("b")) throw new IllegalStateException("the object b could not be read");
            return value;
        }));

        assertThatThrownBy(stream::toList).hasMessage("the object b could not be read");
        assertThat(monitor.health().reason()).contains("the object b could not be read");
    }

    @Test
    void stream_readToTheEnd_keepsThePieceHealthy() {
        PieceMonitor monitor = monitor(Optional.empty());

        assertThat(monitor.stream(() -> Stream.of("a", "b")).toList()).containsExactly("a", "b");
        assertThat(monitor.health().status()).isEqualTo(HealthStatus.HEALTHY);
    }

    @Test
    void run_whenWhatThePieceIsGivenFails_doesNotBlameThePiece() {
        PieceMonitor monitor = monitor(Optional.empty());
        Stream<String> input = Stream.of("a").map(value -> {
            throw new IllegalStateException("the source could not be read");
        });

        assertThatThrownBy(() -> monitor.run(() -> monitor.given(input).forEach(value -> {
        }))).isInstanceOf(IllegalStateException.class).hasMessage("the source could not be read");
        assertThat(monitor.health().status()).isEqualTo(HealthStatus.HEALTHY);
    }

    private PieceMonitor monitor(Optional<HealthProbe> probe) {
        return new PieceMonitor("lake", "FILE_SYSTEM", probe, POLICY, clock);
    }

    private static final class MovingClock extends Clock {
        private Instant now;

        private MovingClock(Instant now) {
            this.now = now;
        }

        void advance(Duration duration) {
            now = now.plus(duration);
        }

        @Override
        public ZoneOffset getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(java.time.ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }
}
