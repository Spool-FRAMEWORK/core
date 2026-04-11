package software.spool.core.adapter.otel;

import io.opentelemetry.exporter.otlp.http.logs.OtlpHttpLogRecordExporter;
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter;  // ← HTTP, no gRPC
import io.opentelemetry.instrumentation.logback.appender.v1_0.OpenTelemetryAppender;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.logs.export.BatchLogRecordProcessor;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.semconv.ServiceAttributes;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class OTELConfig {

    private static final AtomicBoolean INITIALIZED = new AtomicBoolean(false);

    public static void init(String serviceName) {
        if (!INITIALIZED.compareAndSet(false, true)) return;

        String name     = resolveEnv("SERVICE_NAME", serviceName);
        String tracesEp = resolveEnv("OTEL_TRACES_ENDPOINT", "http://localhost:4318/v1/traces");
        String logsEp   = resolveEnv("OTEL_LOGS_ENDPOINT",   "http://localhost:3100/otlp/v1/logs");

        Resource resource = Resource.getDefault().toBuilder()
                .put(ServiceAttributes.SERVICE_NAME, name)
                .build();

        SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
                .setResource(resource)
                .setIdGenerator(OTELIdGenerator.INSTANCE)
                .addSpanProcessor(BatchSpanProcessor.builder(
                        OtlpHttpSpanExporter.builder().setEndpoint(tracesEp).build()
                ).build())
                .build();

        SdkLoggerProvider loggerProvider = SdkLoggerProvider.builder()
                .setResource(resource)
                .addLogRecordProcessor(BatchLogRecordProcessor.builder(
                        OtlpHttpLogRecordExporter.builder().setEndpoint(logsEp).build()
                ).build())
                .build();

        OpenTelemetrySdk sdk = OpenTelemetrySdk.builder()
                .setTracerProvider(tracerProvider)
                .setLoggerProvider(loggerProvider)
                .buildAndRegisterGlobal();

        OpenTelemetryAppender.install(sdk);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            sdk.getSdkLoggerProvider().forceFlush().join(10, TimeUnit.SECONDS);
            sdk.getSdkTracerProvider().forceFlush().join(10, TimeUnit.SECONDS);
            sdk.close();
        }, "otel-shutdown"));
    }

    private static String resolveEnv(String key, String fallback) {
        String v = System.getenv(key);
        return (v != null && !v.isBlank()) ? v : fallback;
    }
}