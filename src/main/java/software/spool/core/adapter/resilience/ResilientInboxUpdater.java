package software.spool.core.adapter.resilience;

import software.spool.core.adapter.logging.LoggerFactory;
import software.spool.core.exception.InboxUpdateException;
import software.spool.core.model.EnvelopeStatus;
import software.spool.core.model.vo.Envelope;
import software.spool.core.model.vo.IdempotencyKey;
import software.spool.core.port.inbox.InboxUpdater;
import software.spool.core.port.logging.Logger;
import software.spool.core.resilience.CircuitBreaker;
import software.spool.core.resilience.adapter.InMemoryCircuitBreakerStateStore;
import software.spool.core.resilience.control.RetryingExecutor;
import software.spool.core.resilience.control.TransitionEvaluator;
import software.spool.core.resilience.exception.CircuitBreakerOpenException;
import software.spool.core.resilience.model.CircuitBreakerPolicy;
import software.spool.core.resilience.model.CircuitBreakerStatus;
import software.spool.core.resilience.model.RetryPolicy;

import java.time.Duration;
import java.util.Collection;
import java.util.concurrent.Callable;

public class ResilientInboxUpdater implements InboxUpdater {
    private static final Logger LOG = LoggerFactory.getLogger(ResilientInboxUpdater.class);
    private final InboxUpdater delegate;
    private final CircuitBreaker cb;
    private final RetryPolicy retryPolicy;

    public ResilientInboxUpdater(InboxUpdater delegate, CircuitBreaker cb, RetryPolicy retryPolicy) {
        this.delegate = delegate;
        this.cb = cb;
        this.retryPolicy = retryPolicy;
    }

    public static ResilientInboxUpdater of(InboxUpdater delegate) {
        CircuitBreaker cb = buildCircuitBreaker();
        RetryPolicy retryPolicy = RetryPolicy.fixedWithAbort(
                3,
                Duration.ofMillis(500),
                ex -> ex instanceof CircuitBreakerOpenException ||
                        cb.status() == CircuitBreakerStatus.HALF_OPEN
        );
        return new ResilientInboxUpdater(delegate, cb, retryPolicy);
    }

    @Override
    public Collection<Envelope> update(Collection<IdempotencyKey> idempotencyKeys, EnvelopeStatus status)
            throws InboxUpdateException {
        return execute(() -> delegate.update(idempotencyKeys, status), idempotencyKeys);
    }

    private <T> T execute(Callable<T> action, Collection<IdempotencyKey> keys) {
        try {
            return new RetryingExecutor<>(retryPolicy, () -> cb.execute(action)).execute();
        } catch (CircuitBreakerOpenException ex) {
            LOG.warn("RESILIENCE - InboxUpdater · {} » circuit OPEN, rejecting update", cb.id());
            throw new InboxUpdateException(keys, "Circuit breaker is OPEN", ex);
        } catch (InboxUpdateException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new InboxUpdateException(keys, ex.getMessage(), ex);
        }
    }

    private static CircuitBreaker buildCircuitBreaker() {
        CircuitBreakerPolicy policy = CircuitBreakerPolicy.countBased()
                .withFailureRateThreshold(0.50f)
                .withMinimumCalls(4)
                .withSlidingWindowSize(10)
                .withCooldown(Duration.ofSeconds(30))
                .withHalfOpenPermits(4)
                .build();
        return new CircuitBreaker(
                "inbox-updater",
                policy,
                new TransitionEvaluator(),
                InMemoryCircuitBreakerStateStore.instance()
        );
    }
}
