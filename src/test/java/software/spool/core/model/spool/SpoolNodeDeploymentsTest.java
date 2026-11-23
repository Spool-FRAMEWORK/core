package software.spool.core.model.spool;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import software.spool.core.port.health.ModuleHealthPayload;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SpoolNodeDeploymentsTest {
    private SpoolNode node;

    @AfterEach
    void stop() {
        if (node != null) node.stop();
    }

    @Test
    void add_whenAModuleFailsToStart_leavesNothingOfTheDeploymentAndCanBeAddedAgain() throws IOException {
        node = SpoolNode.create(freePort());
        node.start();
        RecordingModule first = new RecordingModule("first", false);

        assertThatThrownBy(() -> node.add("orders", List.of(first, new RecordingModule("broken", true))))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("broken");

        assertThat(first.running.get()).isFalse();
        RecordingModule again = new RecordingModule("again", false);
        node.add("orders", List.of(again));
        assertThat(again.running.get()).isTrue();
        node.remove("orders");
        assertThat(again.running.get()).isFalse();
    }

    private static int freePort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }

    private static final class RecordingModule implements SpoolModule {
        private final String id;
        private final boolean failsToStart;
        private final AtomicBoolean running = new AtomicBoolean();

        RecordingModule(String id, boolean failsToStart) {
            this.id = id;
            this.failsToStart = failsToStart;
        }

        @Override
        public void start(SpoolNode.StartPermit permit) {
            if (failsToStart) throw new IllegalStateException(id + " could not start");
            running.set(true);
        }

        @Override
        public void stop(SpoolNode.StartPermit permit) {
            running.set(false);
        }

        @Override
        public ModuleHealthPayload checkHealth() {
            return ModuleHealthPayload.healthy(id);
        }
    }
}
