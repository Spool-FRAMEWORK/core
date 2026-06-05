package software.spool.core.adapter.otel;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.metrics.Meter;
import software.spool.core.port.metrics.MetricsRegistry;

import java.util.Map;

public class OpenTelemetryMetricsRegistry implements MetricsRegistry {

    private static Meter meter() {
        return GlobalOpenTelemetry.getMeterProvider()
                .meterBuilder("spool.metrics")
                .build();
    }

    @Override
    public CounterMetric counter(String name, String description, String unit) {
        return (value, attributes) ->
                meter().counterBuilder(name)
                        .setDescription(description)
                        .setUnit(unit)
                        .build()
                        .add(value, toAttributes(attributes));
    }

    @Override
    public LongHistogramMetric histogram(String name, String description, String unit) {
        return (value, attributes) ->
                meter().histogramBuilder(name)
                        .ofLongs()
                        .setDescription(description)
                        .setUnit(unit)
                        .build()
                        .record(value, toAttributes(attributes));
    }

    @Override
    public TimerMetric timer(String name, String description, String unit) {
        return new TimerMetric() {
            @Override
            public void record(long durationMs, Map<String, String> attributes) {
                meter().histogramBuilder(name)
                        .setDescription(description)
                        .setUnit("s")
                        .build()
                        .record(durationMs / 1000.0, toAttributes(attributes));
            }

            @Override
            public <T> T record(Map<String, String> attributes, CheckedSupplier<T> supplier) throws Exception {
                long start = System.nanoTime();
                try {
                    return supplier.get();
                } finally {
                    meter().histogramBuilder(name)
                            .setDescription(description)
                            .setUnit("s")
                            .build()
                            .record((System.nanoTime() - start) / 1_000_000_000.0, toAttributes(attributes));
                }
            }

            @Override
            public void record(Map<String, String> attributes, CheckedRunnable runnable) throws Exception {
                long start = System.nanoTime();
                try {
                    runnable.run();
                } finally {
                    meter().histogramBuilder(name)
                            .setDescription(description)
                            .setUnit("s")
                            .build()
                            .record((System.nanoTime() - start) / 1_000_000_000.0, toAttributes(attributes));
                }
            }
        };
    }

    @Override
    public GaugeMetric gauge(String name, String description, String unit) {
        return (delta, attributes) ->
                meter().upDownCounterBuilder(name)
                        .setDescription(description)
                        .setUnit(unit)
                        .build()
                        .add(delta, toAttributes(attributes));
    }

    private static Attributes toAttributes(Map<String, String> values) {
        AttributesBuilder builder = Attributes.builder();
        values.forEach((k, v) -> {
            if (k != null && v != null) builder.put(k, v);
        });
        return builder.build();
    }
}
