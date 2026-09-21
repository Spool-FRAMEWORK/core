package software.spool.core.adapter.otel;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OTELExporterSettingsTest {

    private static final String SECRET = "Bearer s3cr3t-value";

    @Test
    void resolveHeaders_headerFromEnvironment_takesValueOfVariable() {
        OTELExporterSettings settings = OTELExporterSettings.builder()
                .headerFromEnvironment("Authorization", "OTLP_AUTHORIZATION")
                .build();

        Map<String, String> headers = settings.resolveHeaders("TRACES", Map.of("OTLP_AUTHORIZATION", SECRET)::get);

        assertThat(headers).containsEntry("Authorization", SECRET);
    }

    @Test
    void resolveHeaders_missingVariable_failsNamingVariableAndHeader() {
        OTELExporterSettings settings = OTELExporterSettings.builder()
                .headerFromEnvironment("Authorization", "OTLP_AUTHORIZATION")
                .build();

        assertThatThrownBy(() -> settings.resolveHeaders("TRACES", name -> null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("OTLP_AUTHORIZATION")
                .hasMessageContaining("Authorization");
    }

    @Test
    void resolveHeaders_blankVariable_fails() {
        OTELExporterSettings settings = OTELExporterSettings.builder()
                .headerFromEnvironment("Authorization", "OTLP_AUTHORIZATION")
                .build();

        assertThatThrownBy(() -> settings.resolveHeaders("TRACES", Map.of("OTLP_AUTHORIZATION", "  ")::get))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("OTLP_AUTHORIZATION");
    }

    @Test
    void resolveHeaders_noConfiguration_isEmpty() {
        assertThat(OTELExporterSettings.none().resolveHeaders("LOGS", name -> null)).isEmpty();
    }

    @Test
    void resolveHeaders_generalEnvironment_isReadForEverySignal() {
        Map<String, String> environment = Map.of("OTEL_EXPORTER_OTLP_HEADERS", "X-Tenant=acme");

        assertThat(OTELExporterSettings.none().resolveHeaders("TRACES", environment::get)).containsEntry("X-Tenant", "acme");
        assertThat(OTELExporterSettings.none().resolveHeaders("LOGS", environment::get)).containsEntry("X-Tenant", "acme");
        assertThat(OTELExporterSettings.none().resolveHeaders("METRICS", environment::get)).containsEntry("X-Tenant", "acme");
    }

    @Test
    void resolveHeaders_signalEnvironment_isReadOnlyForItsSignal() {
        Map<String, String> environment = Map.of("OTEL_EXPORTER_OTLP_LOGS_HEADERS", "X-Tenant=acme");

        assertThat(OTELExporterSettings.none().resolveHeaders("LOGS", environment::get)).containsEntry("X-Tenant", "acme");
        assertThat(OTELExporterSettings.none().resolveHeaders("TRACES", environment::get)).isEmpty();
    }

    @Test
    void resolveHeaders_precedence_lastSourceWins() {
        Map<String, String> environment = Map.of(
                "OTEL_EXPORTER_OTLP_HEADERS", "X-Level=general,X-General=1",
                "OTEL_EXPORTER_OTLP_TRACES_HEADERS", "X-Level=signal,X-Signal=1",
                "FROM_ENVIRONMENT", "environment");
        OTELExporterSettings settings = OTELExporterSettings.builder()
                .header("X-Level", "literal")
                .headerFromEnvironment("X-Level", "FROM_ENVIRONMENT")
                .build();

        Map<String, String> headers = settings.resolveHeaders("TRACES", environment::get);

        assertThat(headers).containsEntry("X-Level", "environment")
                .containsEntry("X-General", "1")
                .containsEntry("X-Signal", "1");
    }

    @Test
    void resolveHeaders_literalHeader_beatsStandardEnvironment() {
        Map<String, String> environment = Map.of("OTEL_EXPORTER_OTLP_TRACES_HEADERS", "X-Level=signal");
        OTELExporterSettings settings = OTELExporterSettings.builder().header("X-Level", "literal").build();

        assertThat(settings.resolveHeaders("TRACES", environment::get)).containsEntry("X-Level", "literal");
    }

    @Test
    void resolveHeaders_headerNames_areCaseInsensitive() {
        Map<String, String> environment = Map.of("OTEL_EXPORTER_OTLP_HEADERS", "Authorization=old");
        OTELExporterSettings settings = OTELExporterSettings.builder().header("authorization", "new").build();

        Map<String, String> headers = settings.resolveHeaders("TRACES", environment::get);

        assertThat(headers).hasSize(1);
        assertThat(headers.get("AUTHORIZATION")).isEqualTo("new");
    }

    @Test
    void resolveHeaders_percentEncodedValue_isDecodedAndKeepsTrailingEquals() {
        Map<String, String> environment = Map.of(
                "OTEL_EXPORTER_OTLP_HEADERS", "Authorization=Basic%20abc==,X-Scope-OrgID=tenant");

        Map<String, String> headers = OTELExporterSettings.none().resolveHeaders("TRACES", environment::get);

        assertThat(headers).containsEntry("Authorization", "Basic abc==").containsEntry("X-Scope-OrgID", "tenant");
    }

    @Test
    void resolveHeaders_plusInValue_isKeptAsPlus() {
        Map<String, String> environment = Map.of("OTEL_EXPORTER_OTLP_HEADERS", "Authorization=Basic%20a+b/c=");

        assertThat(OTELExporterSettings.none().resolveHeaders("TRACES", environment::get))
                .containsEntry("Authorization", "Basic a+b/c=");
    }

    @Test
    void resolveHeaders_spacesAroundPairs_areTrimmed() {
        Map<String, String> environment = Map.of("OTEL_EXPORTER_OTLP_HEADERS", " X-A = 1 , X-B = 2 ");

        assertThat(OTELExporterSettings.none().resolveHeaders("TRACES", environment::get))
                .containsEntry("X-A", "1")
                .containsEntry("X-B", "2");
    }

    @Test
    void resolveHeaders_pairWithoutEquals_failsWithoutLeakingContent() {
        Map<String, String> environment = Map.of("OTEL_EXPORTER_OTLP_HEADERS", "Authorization " + SECRET);

        assertThatThrownBy(() -> OTELExporterSettings.none().resolveHeaders("TRACES", environment::get))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("OTEL_EXPORTER_OTLP_HEADERS")
                .hasMessageNotContaining("s3cr3t");
    }

    @Test
    void resolveHeaders_pairWithoutName_fails() {
        Map<String, String> environment = Map.of("OTEL_EXPORTER_OTLP_TRACES_HEADERS", "=value");

        assertThatThrownBy(() -> OTELExporterSettings.none().resolveHeaders("TRACES", environment::get))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("OTEL_EXPORTER_OTLP_TRACES_HEADERS");
    }

    @Test
    void constructor_unsupportedCompression_fails() {
        assertThatThrownBy(() -> OTELExporterSettings.builder().compression("zstd").build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("zstd");
    }

    @Test
    void constructor_supportedCompression_isAccepted() {
        assertThat(OTELExporterSettings.builder().compression("gzip").build().compression()).isEqualTo("gzip");
        assertThat(OTELExporterSettings.builder().compression("none").build().compression()).isEqualTo("none");
    }

    @Test
    void overriding_signalValuesWin_andHeadersAreMerged() {
        OTELExporterSettings common = OTELExporterSettings.builder()
                .endpoint("http://common")
                .header("X-Common", "1")
                .header("X-Shared", "common")
                .headerFromEnvironment("Authorization", "COMMON_AUTHORIZATION")
                .compression("gzip")
                .timeout(Duration.ofSeconds(5))
                .build();
        OTELExporterSettings signal = OTELExporterSettings.builder()
                .endpoint("http://signal")
                .header("X-Shared", "signal")
                .headerFromEnvironment("Authorization", "SIGNAL_AUTHORIZATION")
                .compression("none")
                .timeout(Duration.ofSeconds(1))
                .build();

        OTELExporterSettings merged = signal.overriding(common);

        assertThat(merged.endpoint()).isEqualTo("http://signal");
        assertThat(merged.headers()).containsEntry("X-Common", "1").containsEntry("X-Shared", "signal");
        assertThat(merged.headersFromEnvironment()).containsEntry("Authorization", "SIGNAL_AUTHORIZATION");
        assertThat(merged.compression()).isEqualTo("none");
        assertThat(merged.timeout()).isEqualTo(Duration.ofSeconds(1));
    }

    @Test
    void overriding_missingValues_areInheritedFromBase() {
        OTELExporterSettings common = OTELExporterSettings.builder()
                .endpoint("http://common")
                .compression("gzip")
                .timeout(Duration.ofSeconds(5))
                .build();

        OTELExporterSettings merged = OTELExporterSettings.none().overriding(common);

        assertThat(merged.endpoint()).isEqualTo("http://common");
        assertThat(merged.compression()).isEqualTo("gzip");
        assertThat(merged.timeout()).isEqualTo(Duration.ofSeconds(5));
    }

    @Test
    void withEndpoint_nullOrBlank_returnsSameInstance() {
        OTELExporterSettings settings = OTELExporterSettings.none();

        assertThat(settings.withEndpoint(null)).isSameAs(settings);
        assertThat(settings.withEndpoint("  ")).isSameAs(settings);
    }

    @Test
    void withEndpoint_value_replacesEndpointAndKeepsTheRest() {
        OTELExporterSettings settings = OTELExporterSettings.builder().header("X-A", "1").compression("gzip").build();

        OTELExporterSettings changed = settings.withEndpoint("http://other");

        assertThat(changed.endpoint()).isEqualTo("http://other");
        assertThat(changed.headers()).containsEntry("X-A", "1");
        assertThat(changed.compression()).isEqualTo("gzip");
    }

    @Test
    void constructor_headers_areCopiedAndImmutable() {
        Map<String, String> source = new HashMap<>();
        source.put("X-A", "1");

        OTELExporterSettings settings = new OTELExporterSettings(null, source, null, null, null);
        source.put("X-B", "2");

        assertThat(settings.headers()).containsOnlyKeys("X-A");
        assertThatThrownBy(() -> settings.headers().put("X-C", "3")).isInstanceOf(UnsupportedOperationException.class);
        assertThat(settings.headersFromEnvironment()).isEmpty();
    }

    @Test
    void toString_neverContainsHeaderValues() {
        OTELExporterSettings settings = OTELExporterSettings.builder()
                .endpoint("http://collector")
                .header("Authorization", SECRET)
                .headerFromEnvironment("X-Token", "TOKEN_VARIABLE")
                .build();

        assertThat(settings.toString())
                .contains("http://collector", "Authorization", "X-Token", "TOKEN_VARIABLE")
                .doesNotContain("s3cr3t");
    }
}
