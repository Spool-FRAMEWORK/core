package software.spool.core.adapter.health;

import org.junit.jupiter.api.Test;
import software.spool.core.model.spool.SpoolModule;
import software.spool.core.model.spool.SpoolNode;
import software.spool.core.port.health.HealthCheck;
import software.spool.core.port.health.HealthProvider;
import software.spool.core.port.health.HealthStatus;
import software.spool.core.port.health.ModuleHealthPayload;
import software.spool.core.port.health.PieceCheckPolicy;
import software.spool.core.port.health.PieceHealth;
import software.spool.core.port.health.PieceMonitor;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class MonitoredModuleTest {
    private final PieceMonitor lake = PieceMonitor.of("lake", "FILE_SYSTEM", Optional.empty(), PieceCheckPolicy.defaults());
    private final PieceMonitor bus = PieceMonitor.of("bus", "IN_MEMORY",
            Optional.of(() -> HealthCheck.unhealthy("bus", "the broker did not answer")), PieceCheckPolicy.defaults());

    @Test
    void checkHealth_addsThePiecesTheModuleUses() {
        SpoolModule module = new MonitoredModule(healthyModule(), List.of(lake));

        assertThat(module.checkHealth().pieces()).extracting(PieceHealth::piece).containsExactly("lake");
        assertThat(module.checkHealth().status()).isEqualTo(HealthStatus.HEALTHY);
    }

    @Test
    void checkHealth_isAsBadAsItsWorstPiece() {
        SpoolModule module = new MonitoredModule(healthyModule(), List.of(lake, bus));

        assertThat(module.checkHealth().status()).isEqualTo(HealthStatus.UNHEALTHY);
    }

    @Test
    void new_tellsTheHeartbeatOfTheModuleToReportThePiecesToo() {
        AtomicReference<HealthProvider> reported = new AtomicReference<>();
        SpoolModule inner = new SpoolModule() {
            @Override
            public void start(SpoolNode.StartPermit permit) {
            }

            @Override
            public void stop(SpoolNode.StartPermit permit) {
            }

            @Override
            public ModuleHealthPayload checkHealth() {
                return ModuleHealthPayload.healthy("payments-silver");
            }

            @Override
            public void reportHealthFrom(HealthProvider health) {
                reported.set(health);
            }
        };

        new MonitoredModule(inner, List.of(lake));

        assertThat(reported.get().checkHealth().pieces()).extracting(PieceHealth::piece).containsExactly("lake");
    }

    private static SpoolModule healthyModule() {
        return new SpoolModule() {
            @Override
            public void start(SpoolNode.StartPermit permit) {
            }

            @Override
            public void stop(SpoolNode.StartPermit permit) {
            }

            @Override
            public ModuleHealthPayload checkHealth() {
                return ModuleHealthPayload.healthy("payments-silver");
            }
        };
    }
}
