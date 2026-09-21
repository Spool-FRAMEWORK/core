package software.spool.core.adapter.resilience;

import org.junit.jupiter.api.Test;
import software.spool.core.model.EnvelopeStatus;
import software.spool.core.model.vo.Envelope;
import software.spool.core.port.inbox.InboxStatusQuery;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class ResilientInboxStatusQueryTest {

    private static final Instant LIMIT = Instant.parse("2026-09-10T00:00:00Z");

    @Test
    void findByStatusModifiedBefore_delegatesInsteadOfFilteringWhatFindByStatusReturns() {
        AtomicInteger everythingReads = new AtomicInteger();
        AtomicInteger filteredReads = new AtomicInteger();
        InboxStatusQuery delegate = new InboxStatusQuery() {
            @Override
            public Collection<Envelope> findByStatus(EnvelopeStatus status) {
                everythingReads.incrementAndGet();
                return List.of();
            }

            @Override
            public Collection<Envelope> findByStatusModifiedBefore(EnvelopeStatus status, Instant limit) {
                filteredReads.incrementAndGet();
                return List.of();
            }
        };

        ResilientInboxStatusQuery.of(delegate).findByStatusModifiedBefore(EnvelopeStatus.PERSISTED, LIMIT);

        assertThat(filteredReads).hasValue(1);
        assertThat(everythingReads).hasValue(0);
    }
}
