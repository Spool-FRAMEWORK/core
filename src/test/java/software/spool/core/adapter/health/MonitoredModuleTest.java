package software.spool.core.adapter.health;

import org.junit.jupiter.api.Test;
import software.spool.core.model.spool.SpoolModule;
import software.spool.core.model.spool.SpoolNode;
import software.spool.core.port.health.HealthCheck;
import software.spool.core.port.health.HealthStatus;
import software.spool.core.port.health.ModuleHealthPayload;
import software.spool.core.port.health.PieceCheckPolicy;
import software.spool.core.port.health.PieceHealth;
import software.spool.core.port.health.PieceMonitor;

import java.util.List;
import java.util.Optional;

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
