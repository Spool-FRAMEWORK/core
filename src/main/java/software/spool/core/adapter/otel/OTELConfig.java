package software.spool.core.adapter.otel;

import io.opentelemetry.exporter.otlp.http.logs.OtlpHttpLogRecordExporter;
import io.opentelemetry.exporter.otlp.http.metrics.OtlpHttpMetricExporter;
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter;
import io.opentelemetry.instrumentation.logback.appender.v1_0.OpenTelemetryAppender;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.logs.export.BatchLogRecordProcessor;
import io.opentelemetry.sdk.metrics.Aggregation;
import io.opentelemetry.sdk.metrics.InstrumentSelector;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.View;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.semconv.ServiceAttributes;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class OTELConfig {
    private static final AtomicBoolean INITIALIZED = new AtomicBoolean(false);

    public static void init(String serviceName) {
        init(serviceName, null, null, null);
    }

    public static void init(String serviceName,
                            String tracesEndpointOverride,
                            String logsEndpointOverride,
                            String metricsEndpointOverride) {
        if (!INITIALIZED.compareAndSet(false, true)) return;

        String name = resolveEnv("SERVICE_NAME", serviceName);

        String tracesEp = firstNonBlank(
                tracesEndpointOverride,
                resolveEnv("OTEL_TRACES_ENDPOINT", "http://localhost:4318/v1/traces")
        );
        String logsEp = firstNonBlank(
                logsEndpointOverride,
                resolveEnv("OTEL_LOGS_ENDPOINT", "http://localhost:3100/otlp/v1/logs")
        );
        String metricsEp = firstNonBlank(
                metricsEndpointOverride,
                resolveEnv("OTEL_METRICS_ENDPOINT", "http://localhost:4320/v1/metrics")
        );

        Resource resource = Resource.getDefault().toBuilder()
                .put(ServiceAttributes.SERVICE_NAME, name)
                .build();

        OtlpHttpSpanExporter spanExporter = OtlpHttpSpanExporter.builder()
                .setEndpoint(tracesEp)
                .build();

        OtlpHttpLogRecordExporter logExporter = OtlpHttpLogRecordExporter.builder()
                .setEndpoint(logsEp)
                .build();

        OtlpHttpMetricExporter metricExporter = OtlpHttpMetricExporter.builder()
                .setEndpoint(metricsEp)
                .build();

        OpenTelemetrySdk sdk = OpenTelemetrySdk.builder()
                .setTracerProvider(SdkTracerProvider.builder()
                        .setResource(resource)
                        .setIdGenerator(OTELIdGenerator.INSTANCE)
                        .addSpanProcessor(BatchSpanProcessor.builder(spanExporter).build())
                        .build())
                .setLoggerProvider(SdkLoggerProvider.builder()
                        .setResource(resource)
                        .addLogRecordProcessor(BatchLogRecordProcessor.builder(logExporter).build())
                        .build())
                .setMeterProvider(SdkMeterProvider.builder()
                        .setResource(resource)
                        .registerView(
                                InstrumentSelector.builder()
                                        .setType(InstrumentType.HISTOGRAM)
                                        .setName("spool.*.latency*")
                                        .build(),
                                View.builder()
                                        .setAggregation(Aggregation.explicitBucketHistogram(
                                                List.of(0.001, 0.005, 0.01, 0.025, 0.05, 0.1, 0.25, 0.5, 1.0, 2.5, 5.0, 10.0)
                                        ))
                                        .build()
                        )
                        .registerView(
                                InstrumentSelector.builder()
                                        .setType(InstrumentType.HISTOGRAM)
                                        .setName("spool.*.duration*")
                                        .build(),
                                View.builder()
                                        .setAggregation(Aggregation.explicitBucketHistogram(
                                                List.of(0.001, 0.005, 0.01, 0.025, 0.05, 0.1, 0.25, 0.5, 1.0, 2.5, 5.0, 10.0)
                                        ))
                                        .build()
                        )
                        .registerView(
                                InstrumentSelector.builder()
                                        .setType(InstrumentType.HISTOGRAM)
                                        .setName("spool.*.size*")
                                        .build(),
                                View.builder()
                                        .setAggregation(Aggregation.explicitBucketHistogram(
                                                List.of(256.0, 512.0, 1024.0, 4096.0, 16384.0, 65536.0, 262144.0, 1048576.0)
                                        ))
                                        .build()
                        )
                        .registerMetricReader(PeriodicMetricReader.builder(metricExporter)
                                .setInterval(Duration.ofSeconds(5))
                                .build())
                        .build())
                .buildAndRegisterGlobal();

        OpenTelemetryAppender.install(sdk);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            sdk.getSdkLoggerProvider().forceFlush().join(10, TimeUnit.SECONDS);
            sdk.getSdkTracerProvider().forceFlush().join(10, TimeUnit.SECONDS);
            sdk.getSdkMeterProvider().forceFlush().join(10, TimeUnit.SECONDS);
            sdk.close();
        }, "otel-shutdown"));
    }

    private static String resolveEnv(String key, String fallback) {
        String v = System.getenv(key);
        return (v != null && !v.isBlank()) ? v : fallback;
    }

    private static String firstNonBlank(String primary, String fallback) {
        if (primary != null && !primary.isBlank()) return primary;
        return fallback;
    }
}