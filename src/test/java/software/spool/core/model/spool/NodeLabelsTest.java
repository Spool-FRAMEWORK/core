package software.spool.core.model.spool;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NodeLabelsTest {

    @Test
    void of_labelsWrittenAsKeyEqualsValue_keepsEachOne() {
        assertThat(NodeLabels.of(List.of("region=eu-west-1", "cloud=aws")).asList())
                .containsExactly("cloud=aws", "region=eu-west-1");
    }

    @Test
    void parse_labelsSeparatedByCommas_keepsEachOne() {
        assertThat(NodeLabels.parse("cloud=aws, region=eu-west-1").asList()).containsExactly("cloud=aws", "region=eu-west-1");
    }

    @Test
    void parse_blankText_hasNoLabels() {
        assertThat(NodeLabels.parse(" ")).isEqualTo(NodeLabels.none());
    }

    @Test
    void of_labelWithoutEquals_failsNamingTheLabel() {
        assertThatThrownBy(() -> NodeLabels.of(List.of("aws")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("aws")
                .hasMessageContaining("key=value");
    }

    @Test
    void of_labelWithAnEmptyKeyOrValue_fails() {
        assertThatThrownBy(() -> NodeLabels.of(List.of("cloud="))).hasMessageContaining("key=value");
        assertThatThrownBy(() -> NodeLabels.of(List.of("=aws"))).hasMessageContaining("key=value");
    }

    @Test
    void of_sameKeyTwiceWithDifferentValues_failsNamingTheKey() {
        assertThatThrownBy(() -> NodeLabels.of(List.of("cloud=aws", "cloud=azure")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cloud");
    }

    @Test
    void include_everyAskedLabelPresent_isTrue() {
        assertThat(NodeLabels.parse("cloud=aws,region=eu-west-1").include(NodeLabels.parse("cloud=aws"))).isTrue();
    }

    @Test
    void include_askedLabelWithAnotherValue_isFalse() {
        assertThat(NodeLabels.parse("cloud=aws").include(NodeLabels.parse("cloud=azure"))).isFalse();
    }

    @Test
    void include_noLabelsAsked_isTrueForAnyNode() {
        assertThat(NodeLabels.none().include(NodeLabels.none())).isTrue();
    }

    @Test
    void toString_writesTheLabelsSeparatedByCommas() {
        assertThat(NodeLabels.parse("region=eu-west-1,cloud=aws")).hasToString("cloud=aws,region=eu-west-1");
    }
}
