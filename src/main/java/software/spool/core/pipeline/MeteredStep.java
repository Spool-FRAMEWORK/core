package software.spool.core.pipeline;

import software.spool.core.port.metrics.MetricsRegistry;

import javax.management.AttributeNotFoundException;
import java.util.Map;
import java.util.function.Function;

public class MeteredStep<I, O> implements Step<I, O> {
    private final Step<I, O> delegate;
    private final MetricsRegistry.CounterMetric successCounter;
    private final MetricsRegistry.CounterMetric errorCounter;
    private final MetricsRegistry.TimerMetric timer;
    private final Function<I, Map<String, String>> attributesExtractor;

    public MeteredStep(Step<I, O> delegate, MetricsRegistry.CounterMetric successCounter, MetricsRegistry.CounterMetric errorCounter, MetricsRegistry.TimerMetric timer, Function<I, Map<String, String>> attributesExtractor) {
        this.delegate = delegate;
        this.successCounter = successCounter;
        this.errorCounter = errorCounter;
        this.timer = timer;
        this.attributesExtractor = attributesExtractor;
    }

    @Override
    public O apply(I input) throws AttributeNotFoundException {
        Map<String, String> attrs = attributesExtractor != null ? attributesExtractor.apply(input) : Map.of();
        long start = System.nanoTime();
        try {
            O result = delegate.apply(input);
            long elapsed = (System.nanoTime() - start) / 1_000_000;
            successCounter.increment(attrs);
            timer.record(elapsed, attrs);
            return result;
        } catch (Exception e) {
            long elapsed = (System.nanoTime() - start) / 1_000_000;
            errorCounter.increment(attrs);
            timer.record(elapsed, attrs);
            throw e;
        }
    }
}
