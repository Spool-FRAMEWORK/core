package software.spool.core.adapter.otel;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.LongHistogram;
import io.opentelemetry.api.metrics.Meter;
import software.spool.core.port.metrics.MetricsRegistry;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class OpenTelemetryMetricsRegistry implements MetricsRegistry {
    private final Meter meter;
    private final ConcurrentHashMap<String, LongCounter> counters = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, LongHistogram> histograms = new ConcurrentHashMap<>();

    public OpenTelemetryMetricsRegistry() {
        this.meter = GlobalOpenTelemetry.getMeterProvider()
                .meterBuilder("spool.metrics")
                .build();
    }

    @Override
    public CounterMetric counter(String name, String description, String unit) {
        LongCounter counter = counters.computeIfAbsent(name, key ->
                meter.counterBuilder(name)
                        .setDescription(description)
                        .setUnit(unit)
                        .build()
        );

        return (value, attributes) -> counter.add(value, toAttributes(attributes));
    }

    @Override
    public LongHistogramMetric histogram(String name, String description, String unit) {
        LongHistogram histogram = histograms.computeIfAbsent(name, key ->
                meter.histogramBuilder(name)
                        .ofLongs()
                        .setDescription(description)
                        .setUnit(unit)
                        .build()
        );

        return (value, attributes) -> histogram.record(value, toAttributes(attributes));
    }

    @Override
    public TimerMetric timer(String name, String description, String unit) {
        LongHistogram histogram = histograms.computeIfAbsent(name, key ->
                meter.histogramBuilder(name)
                        .ofLongs()
                        .setDescription(description)
                        .setUnit(unit)
                        .build()
        );

        return new TimerMetric() {
            @Override
            public void record(long durationMs, Map<String, String> attributes) {
                histogram.record(durationMs, toAttributes(attributes));
            }

            @Override
            public <T> T record(Map<String, String> attributes, CheckedSupplier<T> supplier) throws Exception {
                long start = System.nanoTime();
                try {
                    return supplier.get();
                } finally {
                    long durationMs = (System.nanoTime() - start) / 1_000_000;
                    histogram.record(durationMs, toAttributes(attributes));
                }
            }

            @Override
            public void record(Map<String, String> attributes, CheckedRunnable runnable) throws Exception {
                long start = System.nanoTime();
                try {
                    runnable.run();
                } finally {
                    long durationMs = (System.nanoTime() - start) / 1_000_000;
                    histogram.record(durationMs, toAttributes(attributes));
                }
            }
        };
    }

    private static Attributes toAttributes(Map<String, String> values) {
        AttributesBuilder builder = Attributes.builder();
        values.forEach((k, v) -> {
            if (k != null && v != null) builder.put(k, v);
        });
        return builder.build();
    }
}