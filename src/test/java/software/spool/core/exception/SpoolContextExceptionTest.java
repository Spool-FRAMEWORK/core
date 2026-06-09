package software.spool.core.exception;

import org.junit.jupiter.api.Test;
import software.spool.core.model.fixture.TestEvent;

import static org.assertj.core.api.Assertions.assertThat;

class SpoolContextExceptionTest {

    @Test
    void context_returnsSpoolEventPassedOnConstruction() {
        TestEvent event = TestEvent.sample();
        SpoolContextException ex = new SpoolContextException(new RuntimeException("cause"), event);
        assertThat(ex.context()).isSameAs(event);
    }

    @Test
    void original_returnsWrappedCause() {
        RuntimeException cause = new RuntimeException("root");
        SpoolContextException ex = new SpoolContextException(cause, TestEvent.sample());
        assertThat(ex.original()).isSameAs(cause);
    }

    @Test
    void getMessage_includesCauseMessage() {
        SpoolContextException ex = new SpoolContextException(
                new RuntimeException("the-root-cause"),
                TestEvent.sample()
        );
        assertThat(ex.getMessage()).contains("the-root-cause");
    }
}
