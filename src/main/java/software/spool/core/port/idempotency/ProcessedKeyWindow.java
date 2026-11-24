package software.spool.core.port.idempotency;

import software.spool.core.model.vo.IdempotencyKey;
import software.spool.core.model.vo.ProcessedKey;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * The processed keys a consumer remembers during its window (ADR-007, ADR-033): it keeps each key with the moment it
 * was processed and, before answering, forgets what was processed longer than a window ago.
 */
public final class ProcessedKeyWindow {
    /** Longer than the default threshold of the janitor, so an envelope it republishes while it waits counts as repeated. */
    public static final Duration DEFAULT_WINDOW = Duration.ofMinutes(10);

    private final ProcessedKeyRepository repository;
    private final Duration window;
    private final Clock clock;

    public ProcessedKeyWindow(ProcessedKeyRepository repository, Duration window, Clock clock) {
        if (window.isNegative() || window.isZero())
            throw new IllegalArgumentException("The processed keys window must be longer than zero, got " + window);
        this.repository = Objects.requireNonNull(repository);
        this.window = window;
        this.clock = Objects.requireNonNull(clock);
    }

    public static ProcessedKeyWindow of(ProcessedKeyRepository repository, Duration window) {
        return new ProcessedKeyWindow(repository, window, Clock.systemUTC());
    }

    public boolean remembers(IdempotencyKey key) {
        forgetExpired();
        return repository.contains(key);
    }

    /** Remembers the key as processed now. */
    public void remember(IdempotencyKey key) {
        remember(key, clock.instant());
    }

    /** Remembers the key as processed at the given moment, from which its window counts. */
    public void remember(IdempotencyKey key, Instant processedAt) {
        repository.save(new ProcessedKey(key, processedAt));
    }

    /** Forgets what was processed longer than a window ago. */
    public void forgetExpired() {
        repository.forgetProcessedBefore(clock.instant().minus(window));
    }
}
