package software.spool.core.pipeline;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PipelineTest {

    @Test
    void execute_singleStep_returnsOkResult() {
        Pipeline<Integer, String> pipeline = Pipeline.<Integer>start()
                .add(n -> "value-" + n);

        Result<String> result = pipeline.execute(42);

        assertThat(result).isInstanceOf(Result.Ok.class);
        assertThat(((Result.Ok<String>) result).value()).isEqualTo("value-42");
    }

    @Test
    void execute_multipleSteps_executedInOrder() {
        List<String> order = new ArrayList<>();
        Pipeline<String, String> pipeline = Pipeline.<String>start()
                .add(s -> { order.add("step1:" + s); return s + "-1"; })
                .add(s -> { order.add("step2:" + s); return s + "-2"; });

        Result<String> result = pipeline.execute("start");

        assertThat(result).isInstanceOf(Result.Ok.class);
        assertThat(((Result.Ok<String>) result).value()).isEqualTo("start-1-2");
        assertThat(order).containsExactly("step1:start", "step2:start-1");
    }

    @Test
    void execute_stepThrowsException_returnsErrorAndShortCircuits() {
        List<String> order = new ArrayList<>();
        RuntimeException boom = new RuntimeException("boom");
        Pipeline<String, String> pipeline = Pipeline.<String>start()
                .<String>add(s -> { throw boom; })
                .add(s -> { order.add("should-not-run"); return s; });

        Result<String> result = pipeline.execute("start");

        assertThat(result).isInstanceOf(Result.Error.class);
        assertThat(((Result.Error<String>) result).error()).isSameAs(boom);
        assertThat(order).isEmpty();
    }

    @Test
    void execute_emptyPipeline_returnsInitialInput() {
        Pipeline<String, String> pipeline = Pipeline.start();

        Result<String> result = pipeline.execute("hello");

        assertThat(result).isInstanceOf(Result.Ok.class);
        assertThat(((Result.Ok<String>) result).value()).isEqualTo("hello");
    }
}
