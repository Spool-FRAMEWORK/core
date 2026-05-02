package software.spool.core.circuitbreaker.control;

import software.spool.core.circuitbreaker.model.RetryPolicy;

import java.time.Duration;
import java.util.concurrent.Callable;

public record RetryingExecutor<T>(
        RetryPolicy policy,
        Callable<T> delegate
) {
    public T execute() throws Exception {
        int attempt = 0;
        while (true) {
            try {
                return delegate.call();
            } catch (Exception ex) {
                if (!policy.canRetry(attempt, ex)) throw ex;
                Duration delay = policy.getDelayOf(attempt);
                if (!delay.isZero()) Thread.sleep(delay.toMillis());
                attempt++;
            }
        }
    }
}
