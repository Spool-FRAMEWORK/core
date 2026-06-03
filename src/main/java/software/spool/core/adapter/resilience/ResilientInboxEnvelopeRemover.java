package software.spool.core.adapter.resilience;

import software.spool.core.adapter.logging.LoggerFactory;
import software.spool.core.exception.InboxWriteException;
import software.spool.core.model.vo.Envelope;
import software.spool.core.model.vo.IdempotencyKey;
import software.spool.core.port.inbox.InboxEnvelopeRemover;
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

public class ResilientInboxEnvelopeRemover implements InboxEnvelopeRemover {
    private static final Logger LOG = LoggerFactory.getLogger(ResilientInboxEnvelopeRemover.class);
    private final InboxEnvelopeRemover delegate;
    private final CircuitBreaker cb;
    private final RetryPolicy retryPolicy;

    public ResilientInboxEnvelopeRemover(InboxEnvelopeRemover delegate, CircuitBreaker cb, RetryPolicy retryPolicy) {
        this.delegate = delegate;
        this.cb = cb;
        this.retryPolicy = retryPolicy;
    }

    public static ResilientInboxEnvelopeRemover of(InboxEnvelopeRemover delegate) {
        CircuitBreaker cb = buildCircuitBreaker();
        RetryPolicy retryPolicy = RetryPolicy.fixedWithAbort(
                3,
                Duration.ofMillis(500),
                ex -> ex instanceof CircuitBreakerOpenException ||
                        cb.status() == CircuitBreakerStatus.HALF_OPEN
        );
        return new ResilientInboxEnvelopeRemover(delegate, cb, retryPolicy);
    }

    @Override
    public Collection<Envelope> remove(Collection<IdempotencyKey> idempotencyKeys) {
        return execute(() -> delegate.remove(idempotencyKeys), idempotencyKeys.toString());
    }

    private <T> T execute(Callable<T> action, String context) {
        try {
            return new RetryingExecutor<>(retryPolicy, () -> cb.execute(action)).execute();
        } catch (CircuitBreakerOpenException ex) {
            LOG.warn("RESILIENCE - InboxEnvelopeRemover · {} » circuit OPEN, rejecting remove [{}]",
                    cb.id(), context);
            throw new InboxWriteException("Circuit breaker is OPEN: " + context, ex);
        } catch (InboxWriteException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new InboxWriteException(ex.getMessage(), ex);
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
                "inbox-envelope-remover",
                policy,
                new TransitionEvaluator(),
                InMemoryCircuitBreakerStateStore.instance()
        );
    }
}
