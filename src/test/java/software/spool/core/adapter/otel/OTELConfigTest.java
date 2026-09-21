package software.spool.core.adapter.otel;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OTELConfigTest {

    @Test
    void init_invalidSettings_failsAgainOnRetryInsteadOfStayingInitialized() {
        OTELExporterSettings invalid = OTELExporterSettings.builder().headerFromEnvironment("Authorization", "OTLP_AUTHORIZATION").build();
        OTELExporterSettings none = OTELExporterSettings.none();

        assertThatThrownBy(() -> OTELConfig.init("test", invalid, none, none, name -> null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("OTLP_AUTHORIZATION");
        assertThatThrownBy(() -> OTELConfig.init("test", invalid, none, none, name -> null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("OTLP_AUTHORIZATION");
    }
}
