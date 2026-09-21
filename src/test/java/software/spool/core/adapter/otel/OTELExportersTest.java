package software.spool.core.adapter.otel;

import com.sun.net.httpserver.HttpServer;
import io.opentelemetry.exporter.otlp.http.logs.OtlpHttpLogRecordExporter;
import io.opentelemetry.exporter.otlp.http.metrics.OtlpHttpMetricExporter;
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.logs.export.SimpleLogRecordProcessor;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OTELExportersTest {

    private static final String TOKEN = "Bearer secreto";

    private record Received(String path, String authorization, String contentEncoding, String tenant) {}

    private final List<Received> received = new CopyOnWriteArrayList<>();
    private volatile boolean requireAuthorization = true;
    private HttpServer server;

    @BeforeEach
    void startCollector() throws IOException {
        server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
        server.createContext("/", exchange -> {
            String authorization = exchange.getRequestHeaders().getFirst("Authorization");
            received.add(new Received(
                    exchange.getRequestURI().getPath(),
                    authorization,
                    exchange.getRequestHeaders().getFirst("Content-Encoding"),
                    exchange.getRequestHeaders().getFirst("X-Scope-OrgID")));
            exchange.getRequestBody().readAllBytes();
            boolean accepted = !requireAuthorization || TOKEN.equals(authorization);
            exchange.sendResponseHeaders(accepted ? 200 : 401, -1);
            exchange.close();
        });
        server.start();
    }

    @AfterEach
    void stopCollector() {
        server.stop(0);
    }

    private String endpoint(String path) {
        return "http://127.0.0.1:" + server.getAddress().getPort() + path;
    }

    private static OTELExporterSettings authorizationFromEnvironment() {
        return OTELExporterSettings.builder().headerFromEnvironment("Authorization", "OTLP_AUTHORIZATION").build();
    }

    private static boolean exportSpan(OtlpHttpSpanExporter exporter) {
        try (SdkTracerProvider provider = SdkTracerProvider.builder()
                .addSpanProcessor(SimpleSpanProcessor.create(exporter))
                .build()) {
            provider.get("test").spanBuilder("span").startSpan().end();
            return provider.forceFlush().join(5, TimeUnit.SECONDS).isSuccess();
        }
    }

    private static boolean exportLog(OtlpHttpLogRecordExporter exporter) {
        try (SdkLoggerProvider provider = SdkLoggerProvider.builder()
                .addLogRecordProcessor(SimpleLogRecordProcessor.create(exporter))
                .build()) {
            provider.get("test").logRecordBuilder().setBody("log").emit();
            return provider.forceFlush().join(5, TimeUnit.SECONDS).isSuccess();
        }
    }

    private static boolean exportMetric(OtlpHttpMetricExporter exporter) {
        try (SdkMeterProvider provider = SdkMeterProvider.builder()
                .registerMetricReader(PeriodicMetricReader.builder(exporter).setInterval(Duration.ofHours(1)).build())
                .build()) {
            provider.get("test").counterBuilder("counter").build().add(1);
            return provider.forceFlush().join(5, TimeUnit.SECONDS).isSuccess();
        }
    }

    @Test
    void span_headerFromEnvironment_isAcceptedByProtectedCollector() {
        OtlpHttpSpanExporter exporter = OTELExporters.span(endpoint("/v1/traces"), authorizationFromEnvironment(), Map.of("OTLP_AUTHORIZATION", TOKEN)::get);

        assertThat(exportSpan(exporter)).isTrue();
        assertThat(received).extracting(Received::authorization).containsOnly(TOKEN);
    }

    @Test
    void span_withoutHeaders_isRejectedByProtectedCollector() {
        OtlpHttpSpanExporter exporter = OTELExporters.span(endpoint("/v1/traces"), OTELExporterSettings.none(), name -> null);

        assertThat(exportSpan(exporter)).isFalse();
        assertThat(received).hasSize(1);
        assertThat(received.get(0).authorization()).isNull();
    }

    @Test
    void log_headerFromEnvironment_isAcceptedByProtectedCollector() {
        OtlpHttpLogRecordExporter exporter = OTELExporters.log(endpoint("/otlp/v1/logs"), authorizationFromEnvironment(), Map.of("OTLP_AUTHORIZATION", TOKEN)::get);

        assertThat(exportLog(exporter)).isTrue();
        assertThat(received).extracting(Received::authorization).containsOnly(TOKEN);
    }

    @Test
    void log_withoutHeaders_isRejectedByProtectedCollector() {
        OtlpHttpLogRecordExporter exporter = OTELExporters.log(endpoint("/otlp/v1/logs"), OTELExporterSettings.none(), name -> null);

        assertThat(exportLog(exporter)).isFalse();
    }

    @Test
    void metric_headerFromEnvironment_isAcceptedByProtectedCollector() {
        OtlpHttpMetricExporter exporter = OTELExporters.metric(endpoint("/v1/metrics"), authorizationFromEnvironment(), Map.of("OTLP_AUTHORIZATION", TOKEN)::get);

        assertThat(exportMetric(exporter)).isTrue();
        assertThat(received).extracting(Received::authorization).containsOnly(TOKEN);
    }

    @Test
    void metric_withoutHeaders_sendsNoAuthorizationToProtectedCollector() {
        OtlpHttpMetricExporter exporter = OTELExporters.metric(endpoint("/v1/metrics"), OTELExporterSettings.none(), name -> null);

        exportMetric(exporter);

        assertThat(received).isNotEmpty().allSatisfy(request -> assertThat(request.authorization()).isNull());
    }

    @Test
    void span_standardEnvironmentHeaders_areSentWithoutAnyConfiguration() {
        Map<String, String> environment = Map.of("OTEL_EXPORTER_OTLP_HEADERS", "Authorization=Bearer%20secreto");
        OtlpHttpSpanExporter exporter = OTELExporters.span(endpoint("/v1/traces"), OTELExporterSettings.none(), environment::get);

        assertThat(exportSpan(exporter)).isTrue();
        assertThat(received).extracting(Received::authorization).containsOnly(TOKEN);
    }

    @Test
    void span_signalEnvironmentHeaders_areSentOnlyForTraces() {
        Map<String, String> environment = Map.of("OTEL_EXPORTER_OTLP_LOGS_HEADERS", "Authorization=Bearer%20secreto");
        OtlpHttpSpanExporter exporter = OTELExporters.span(endpoint("/v1/traces"), OTELExporterSettings.none(), environment::get);

        assertThat(exportSpan(exporter)).isFalse();
    }

    @Test
    void span_customNonAuthHeader_isSent() {
        requireAuthorization = false;
        OTELExporterSettings settings = OTELExporterSettings.builder().header("X-Scope-OrgID", "tenant-1").build();
        OtlpHttpSpanExporter exporter = OTELExporters.span(endpoint("/v1/traces"), settings, name -> null);

        assertThat(exportSpan(exporter)).isTrue();
        assertThat(received).extracting(Received::tenant).containsOnly("tenant-1");
    }

    @Test
    void span_gzipCompression_setsContentEncoding() {
        requireAuthorization = false;
        OTELExporterSettings settings = OTELExporterSettings.builder().compression("gzip").build();
        OtlpHttpSpanExporter exporter = OTELExporters.span(endpoint("/v1/traces"), settings, name -> null);

        assertThat(exportSpan(exporter)).isTrue();
        assertThat(received).extracting(Received::contentEncoding).containsOnly("gzip");
    }

    @Test
    void span_timeout_isAccepted() {
        requireAuthorization = false;
        OTELExporterSettings settings = OTELExporterSettings.builder().timeout(Duration.ofSeconds(3)).build();
        OtlpHttpSpanExporter exporter = OTELExporters.span(endpoint("/v1/traces"), settings, name -> null);

        assertThat(exportSpan(exporter)).isTrue();
    }

    @Test
    void span_withoutSettings_behavesAsBefore() {
        requireAuthorization = false;
        OtlpHttpSpanExporter exporter = OTELExporters.span(endpoint("/v1/traces"), OTELExporterSettings.none(), name -> null);

        assertThat(exportSpan(exporter)).isTrue();
        assertThat(received).hasSize(1);
        assertThat(received.get(0).path()).isEqualTo("/v1/traces");
        assertThat(received.get(0).authorization()).isNull();
        assertThat(received.get(0).contentEncoding()).isNull();
        assertThat(received.get(0).tenant()).isNull();
    }

    @Test
    void exporters_missingVariable_failBeforeExporting() {
        assertThatThrownBy(() -> OTELExporters.span(endpoint("/v1/traces"), authorizationFromEnvironment(), name -> null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("OTLP_AUTHORIZATION");
        assertThatThrownBy(() -> OTELExporters.log(endpoint("/otlp/v1/logs"), authorizationFromEnvironment(), name -> null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("OTLP_AUTHORIZATION");
        assertThatThrownBy(() -> OTELExporters.metric(endpoint("/v1/metrics"), authorizationFromEnvironment(), name -> null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("OTLP_AUTHORIZATION");
        assertThat(received).isEmpty();
    }
}
