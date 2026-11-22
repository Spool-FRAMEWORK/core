package software.spool.core.adapter.memory;

import software.spool.core.model.vo.IdempotencyKey;
import software.spool.core.port.idempotency.ProcessedKeyRepository;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Objects;

/**
 * Remembers the processed keys in the memory of the process, so they are lost when it restarts and each replica has
 * its own. Keys are kept in the order they were saved, so forgetting the expired ones stops at the first that is not.
 */
public final class InMemoryProcessedKeyRepository implements ProcessedKeyRepository {
    /** Longer than the default threshold of the janitor, so an envelope it republishes while it waits counts as repeated. */
    public static final Duration DEFAULT_WINDOW = Duration.ofMinutes(10);
    private final LinkedHashMap<IdempotencyKey, Instant> savedAt = new LinkedHashMap<>();
    private final Duration window;
    private final Clock clock;

    public InMemoryProcessedKeyRepository(Duration window, Clock clock) {
        if (window.isNegative() || window.isZero())
            throw new IllegalArgumentException("The processed keys window must be longer than zero, got " + window);
        this.window = window;
        this.clock = Objects.requireNonNull(clock);
    }

    public static InMemoryProcessedKeyRepository remembering(Duration window) {
        return new InMemoryProcessedKeyRepository(window, Clock.systemUTC());
    }

    @Override
    public synchronized boolean contains(IdempotencyKey key) {
        forgetExpired();
        return savedAt.containsKey(key);
    }

    @Override
    public synchronized void save(IdempotencyKey key) {
        forgetExpired();
        savedAt.remove(key);
        savedAt.put(key, clock.instant());
    }

    private void forgetExpired() {
        Instant oldestRemembered = clock.instant().minus(window);
        Iterator<Instant> oldestFirst = savedAt.values().iterator();
        while (oldestFirst.hasNext() && !oldestFirst.next().isAfter(oldestRemembered)) oldestFirst.remove();
    }
}
