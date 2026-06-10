package software.spool.core.pipeline;

import org.junit.jupiter.api.Test;

import javax.management.AttributeNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PipelineContextTest {

    private static final ContextKey<String> NAME = ContextKey.of("name");
    private static final ContextKey<Integer> COUNT = ContextKey.of("count");

    @Test
    void require_existingKey_returnsValue() throws AttributeNotFoundException {
        PipelineContext ctx = PipelineContext.empty().with(NAME, "alice");
        assertThat(ctx.require(NAME)).isEqualTo("alice");
    }

    @Test
    void require_missingKey_throwsAttributeNotFoundException() {
        PipelineContext ctx = PipelineContext.empty();
        assertThatThrownBy(() -> ctx.require(NAME))
                .isInstanceOf(AttributeNotFoundException.class);
    }

    @Test
    void with_existingContext_returnsNewContextWithAddedKey() throws AttributeNotFoundException {
        PipelineContext base = PipelineContext.empty().with(NAME, "alice");
        PipelineContext updated = base.with(COUNT, 3);
        assertThat(updated.require(NAME)).isEqualTo("alice");
        assertThat(updated.require(COUNT)).isEqualTo(3);
    }

    @Test
    void empty_returnsContextWithNoAttributes() {
        PipelineContext ctx = PipelineContext.empty();
        assertThatThrownBy(() -> ctx.require(NAME))
                .isInstanceOf(AttributeNotFoundException.class);
    }
}
