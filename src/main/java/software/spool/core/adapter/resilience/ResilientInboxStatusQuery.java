package software.spool.core.adapter.resilience;

import software.spool.core.adapter.logging.LoggerFactory;
import software.spool.core.exception.InboxReadException;
import software.spool.core.model.EnvelopeStatus;
import software.spool.core.model.vo.Envelope;
import software.spool.core.port.inbox.InboxStatusQuery;
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

public class ResilientInboxStatusQuery implements InboxStatusQuery {
    private static final Logger LOG = LoggerFactory.getLogger(ResilientInboxStatusQuery.class);
    private final InboxStatusQuery delegate;
    private final CircuitBreaker cb;
    private final RetryPolicy retryPolicy;

    public ResilientInboxStatusQuery(InboxStatusQuery delegate, CircuitBreaker cb, RetryPolicy retryPolicy) {
        this.delegate = delegate;
        this.cb = cb;
        this.retryPolicy = retryPolicy;
    }

    public static ResilientInboxStatusQuery of(InboxStatusQuery delegate) {
        CircuitBreaker cb = buildCircuitBreaker();
        RetryPolicy retryPolicy = RetryPolicy.fixedWithAbort(
                3,
                Duration.ofMillis(500),
                ex -> ex instanceof CircuitBreakerOpenException ||
                        cb.status() == CircuitBreakerStatus.HALF_OPEN
        );
        return new ResilientInboxStatusQuery(delegate, cb, retryPolicy);
    }

    @Override
    public Collection<Envelope> findByStatus(EnvelopeStatus status) throws InboxReadException {
        return execute(() -> delegate.findByStatus(status), status.name());
    }

    private <T> T execute(Callable<T> action, String context) {
        try {
            return new RetryingExecutor<>(retryPolicy, () -> cb.execute(action)).execute();
        } catch (CircuitBreakerOpenException ex) {
            LOG.warn("RESILIENCE - InboxStatusQuery · {} » circuit OPEN, rejecting query [{}]",
                    cb.id(), context);
            throw new InboxReadException("Circuit breaker is OPEN: " + context, ex);
        } catch (InboxReadException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new InboxReadException(ex.getMessage(), ex);
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
                "inbox-status-query",
                policy,
                new TransitionEvaluator(),
                InMemoryCircuitBreakerStateStore.instance()
        );
    }
}
