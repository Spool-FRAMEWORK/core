package software.spool.core.adapter.otel;

import io.opentelemetry.exporter.otlp.http.logs.OtlpHttpLogRecordExporter;
import io.opentelemetry.exporter.otlp.http.logs.OtlpHttpLogRecordExporterBuilder;
import io.opentelemetry.exporter.otlp.http.metrics.OtlpHttpMetricExporter;
import io.opentelemetry.exporter.otlp.http.metrics.OtlpHttpMetricExporterBuilder;
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter;
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporterBuilder;

import java.time.Duration;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

final class OTELExporters {
    private OTELExporters() {}

    static OtlpHttpSpanExporter span(String endpoint, OTELExporterSettings settings, UnaryOperator<String> environment) {
        OtlpHttpSpanExporterBuilder builder = OtlpHttpSpanExporter.builder().setEndpoint(endpoint);
        apply(settings, "TRACES", environment, builder::setHeaders, builder::setCompression, builder::setTimeout);
        return builder.build();
    }

    static OtlpHttpLogRecordExporter log(String endpoint, OTELExporterSettings settings, UnaryOperator<String> environment) {
        OtlpHttpLogRecordExporterBuilder builder = OtlpHttpLogRecordExporter.builder().setEndpoint(endpoint);
        apply(settings, "LOGS", environment, builder::setHeaders, builder::setCompression, builder::setTimeout);
        return builder.build();
    }

    static OtlpHttpMetricExporter metric(String endpoint, OTELExporterSettings settings, UnaryOperator<String> environment) {
        OtlpHttpMetricExporterBuilder builder = OtlpHttpMetricExporter.builder().setEndpoint(endpoint);
        apply(settings, "METRICS", environment, builder::setHeaders, builder::setCompression, builder::setTimeout);
        return builder.build();
    }

    private static void apply(OTELExporterSettings settings,
                              String signal,
                              UnaryOperator<String> environment,
                              Consumer<Supplier<Map<String, String>>> headers,
                              Consumer<String> compression,
                              Consumer<Duration> timeout) {
        Map<String, String> resolved = settings.resolveHeaders(signal, environment);
        if (!resolved.isEmpty()) headers.accept(() -> resolved);
        if (settings.compression() != null) compression.accept(settings.compression());
        if (settings.timeout() != null) timeout.accept(settings.timeout());
    }
}
