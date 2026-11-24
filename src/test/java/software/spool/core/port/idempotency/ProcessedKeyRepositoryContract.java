package software.spool.core.port.idempotency;

import org.junit.jupiter.api.Test;
import software.spool.core.model.vo.IdempotencyKey;
import software.spool.core.model.vo.ProcessedKey;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Rules every {@link ProcessedKeyRepository} adapter must follow (ADR-007, ADR-033): it keeps each key with the moment
 * it was processed, and forgets only what it is asked to.
 */
public abstract class ProcessedKeyRepositoryContract {
    private static final IdempotencyKey KEY = IdempotencyKey.of("order-1");
    private static final IdempotencyKey OTHER_KEY = IdempotencyKey.of("order-2");
    private static final Instant MONDAY = Instant.parse("2026-11-23T10:00:00Z");
    private static final Instant TUESDAY = Instant.parse("2026-11-24T10:00:00Z");
    private static final Instant WEDNESDAY = Instant.parse("2026-11-25T10:00:00Z");

    protected abstract ProcessedKeyRepository repository();

    @Test
    void contains_keyNeverSaved_isFalse() {
        assertThat(repository().contains(KEY)).isFalse();
    }

    @Test
    void contains_savedKey_isTrue() {
        ProcessedKeyRepository repository = repository();
        repository.save(new ProcessedKey(KEY, MONDAY));

        assertThat(repository.contains(KEY)).isTrue();
        assertThat(repository.contains(OTHER_KEY)).isFalse();
    }

    @Test
    void forgetProcessedBefore_forgetsOnlyTheKeysProcessedBeforeThatMoment() {
        ProcessedKeyRepository repository = repository();
        repository.save(new ProcessedKey(KEY, MONDAY));
        repository.save(new ProcessedKey(OTHER_KEY, WEDNESDAY));

        repository.forgetProcessedBefore(TUESDAY);

        assertThat(repository.contains(KEY)).isFalse();
        assertThat(repository.contains(OTHER_KEY)).isTrue();
    }

    @Test
    void forgetProcessedBefore_keepsAKeyProcessedAtThatVeryMoment() {
        ProcessedKeyRepository repository = repository();
        repository.save(new ProcessedKey(KEY, TUESDAY));

        repository.forgetProcessedBefore(TUESDAY);

        assertThat(repository.contains(KEY)).isTrue();
    }

    @Test
    void save_keyAlreadySaved_keepsTheNewMomentItWasProcessed() {
        ProcessedKeyRepository repository = repository();
        repository.save(new ProcessedKey(KEY, MONDAY));
        repository.save(new ProcessedKey(KEY, WEDNESDAY));

        repository.forgetProcessedBefore(TUESDAY);

        assertThat(repository.contains(KEY)).isTrue();
    }
}
