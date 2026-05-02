package software.spool.core.resilience.control;

import software.spool.core.adapter.logging.LoggerFactory;
import software.spool.core.resilience.model.RetryPolicy;
import software.spool.core.port.logging.Logger;

import java.time.Duration;
import java.util.concurrent.Callable;

public record RetryingExecutor<T>(
        RetryPolicy policy,
        Callable<T> delegate
) {
    private static final Logger LOG = LoggerFactory.getLogger(RetryingExecutor.class);

    public T execute() throws Exception {
        int attempt = 0;
        while (true) {
            try {
                T result = delegate.call();
                if (attempt > 0) {
                    LOG.info("RESILIENCE - Retry · OK  [{}/{}]", attempt + 1, policy.maximumAttempts());
                }
                return result;
            } catch (Exception ex) {
                attempt++;
                if (!policy.canRetry(attempt, ex)) {
                    LOG.error("RESILIENCE - Retry · GAVE UP  [{}/{}]  {}", attempt, policy.maximumAttempts(), ex.getMessage());
                    throw ex;
                }
                Duration delay = policy.getDelayOf(attempt - 1);
                LOG.warn("RESILIENCE - Retry · FAIL  [{}/{}]  {} — next in {}ms",
                        attempt, policy.maximumAttempts(), ex.getMessage(), delay.toMillis());
                if (!delay.isZero()) Thread.sleep(delay.toMillis());
            }
        }
    }
}