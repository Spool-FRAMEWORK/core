package software.spool.core.utils.polling;

import org.junit.jupiter.api.Test;
import software.spool.core.model.schedule.Schedule;
import software.spool.core.model.watchdog.ModuleIdentity;
import software.spool.core.port.health.HealthCheck;
import software.spool.core.port.health.HealthStatus;
import software.spool.core.port.health.ModuleHealthPayload;
import software.spool.core.port.health.PieceHealth;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class PollingHeartbeatTest {
    private static final ModuleIdentity MODULE = ModuleIdentity.of("payments-silver");
    private static final PollingConfiguration RUN_ONCE_NOW = new PollingConfiguration((task, schedule, token) -> task.run(), Schedule.once());

    private final List<ModuleHealthPayload> beats = new ArrayList<>();

    @Test
    void beat_withoutAProviderOfHealth_reportsTheModuleHealthy() {
        new PollingHeartbeat((identity, health) -> beats.add(health), MODULE, RUN_ONCE_NOW).start();

        assertThat(beats).singleElement().satisfies(health -> {
            assertThat(health.moduleId()).isEqualTo("payments-silver");
            assertThat(health.status()).isEqualTo(HealthStatus.HEALTHY);
        });
    }

    @Test
    void beat_sendsTheHealthOfTheProviderItWasGiven() {
        PieceHealth lake = new PieceHealth("lake", "S3", HealthStatus.UNHEALTHY, Optional.of("the bucket does not exist"),
                Instant.parse("2026-11-22T10:00:00Z"), true);
        PollingHeartbeat heartbeat = new PollingHeartbeat((identity, health) -> beats.add(health), MODULE, RUN_ONCE_NOW);
        heartbeat.reportFrom(() -> ModuleHealthPayload.of("payments-silver", List.of(HealthCheck.healthy("own")))
                .withPieces(List.of(lake)));

        heartbeat.start();

        assertThat(beats).singleElement().satisfies(health -> {
            assertThat(health.status()).isEqualTo(HealthStatus.UNHEALTHY);
            assertThat(health.pieces()).containsExactly(lake);
        });
    }
}
