package software.spool.core.port.idempotency;

import org.junit.jupiter.api.Test;
import software.spool.core.model.vo.IdempotencyKey;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Rules every {@link ProcessedKeyRepository} adapter must follow (ADR-007).
 */
public abstract class ProcessedKeyRepositoryContract {
    private static final Duration WINDOW = Duration.ofMinutes(10);
    private static final IdempotencyKey KEY = IdempotencyKey.of("order-1");
    private static final IdempotencyKey OTHER_KEY = IdempotencyKey.of("order-2");

    private final MovableClock clock = new MovableClock();

    protected abstract ProcessedKeyRepository repository(Duration window, Clock clock);

    @Test
    void contains_keyNeverSaved_isFalse() {
        assertThat(repository(WINDOW, clock).contains(KEY)).isFalse();
    }

    @Test
    void contains_keySavedWithinTheWindow_isTrue() {
        ProcessedKeyRepository repository = repository(WINDOW, clock);
        repository.save(KEY);
        clock.advance(WINDOW.minusSeconds(1));

        assertThat(repository.contains(KEY)).isTrue();
        assertThat(repository.contains(OTHER_KEY)).isFalse();
    }

    @Test
    void contains_keySavedAWindowAgo_isFalse() {
        ProcessedKeyRepository repository = repository(WINDOW, clock);
        repository.save(KEY);
        clock.advance(WINDOW);

        assertThat(repository.contains(KEY)).isFalse();
    }

    @Test
    void save_keyAlreadySaved_startsItsWindowAgain() {
        ProcessedKeyRepository repository = repository(WINDOW, clock);
        repository.save(KEY);
        clock.advance(WINDOW.minusMinutes(1));
        repository.save(KEY);
        clock.advance(Duration.ofMinutes(2));

        assertThat(repository.contains(KEY)).isTrue();
    }

    @Test
    void contains_keySavedAfterAnExpiredOne_isStillRemembered() {
        ProcessedKeyRepository repository = repository(WINDOW, clock);
        repository.save(KEY);
        clock.advance(WINDOW.minusMinutes(1));
        repository.save(OTHER_KEY);
        clock.advance(Duration.ofMinutes(2));

        assertThat(repository.contains(KEY)).isFalse();
        assertThat(repository.contains(OTHER_KEY)).isTrue();
    }

    private static final class MovableClock extends Clock {
        private Instant now = Instant.parse("2026-11-22T10:00:00Z");

        void advance(Duration duration) {
            now = now.plus(duration);
        }

        @Override public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override public Instant instant() {
            return now;
        }
    }
}
