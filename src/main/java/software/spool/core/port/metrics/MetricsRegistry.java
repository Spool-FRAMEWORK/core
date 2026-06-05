package software.spool.core.port.metrics;

import java.util.Map;

public interface MetricsRegistry {
    CounterMetric counter(String name, String description, String unit);
    LongHistogramMetric histogram(String name, String description, String unit);
    TimerMetric timer(String name, String description, String unit);
    GaugeMetric gauge(String name, String description, String unit);

    interface CounterMetric {
        void add(long value, Map<String, String> attributes);
        default void increment(Map<String, String> attributes) {
            add(1, attributes);
        }
    }

    interface LongHistogramMetric {
        void record(long value, Map<String, String> attributes);
    }

    interface GaugeMetric {
        void add(long delta, Map<String, String> attributes);
        default void increment(Map<String, String> attributes) { add(1, attributes); }
        default void decrement(Map<String, String> attributes) { add(-1, attributes); }
    }

    interface TimerMetric {
        void record(long durationMs, Map<String, String> attributes);
        <T> T record(Map<String, String> attributes, CheckedSupplier<T> supplier) throws Exception;
        void record(Map<String, String> attributes, CheckedRunnable runnable) throws Exception;
    }

    @FunctionalInterface
    interface CheckedSupplier<T> {
        T get() throws Exception;
    }

    @FunctionalInterface
    interface CheckedRunnable {
        void run() throws Exception;
    }

    MetricsRegistry NOOP = new MetricsRegistry() {
        public CounterMetric counter(String name, String description, String unit) { return (v, a) -> {}; }
        public LongHistogramMetric histogram(String name, String description, String unit) { return (v, a) -> {}; }
        public GaugeMetric gauge(String name, String description, String unit) { return (d, a) -> {}; }
        public TimerMetric timer(String name, String description, String unit) {
            return new TimerMetric() {
                public void record(long durationMs, Map<String, String> attributes) {}
                public <T> T record(Map<String, String> attributes, CheckedSupplier<T> supplier) throws Exception { return supplier.get(); }
                public void record(Map<String, String> attributes, CheckedRunnable runnable) throws Exception { runnable.run(); }
            };
        }
    };
}