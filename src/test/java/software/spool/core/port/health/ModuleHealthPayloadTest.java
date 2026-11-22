package software.spool.core.port.health;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class ModuleHealthPayloadTest {
    private static final Instant NOW = Instant.parse("2026-11-22T10:00:00Z");

    @Test
    void withPieces_keepsThePiecesOfTheModule() {
        ModuleHealthPayload payload = ModuleHealthPayload.healthy("payments-silver").withPieces(List.of(piece("lake", HealthStatus.HEALTHY)));

        assertThat(payload.pieces()).extracting(PieceHealth::piece).containsExactly("lake");
        assertThat(payload.status()).isEqualTo(HealthStatus.HEALTHY);
    }

    @Test
    void withPieces_takesTheWorstStatusOfTheModuleAndItsPieces() {
        ModuleHealthPayload payload = ModuleHealthPayload.healthy("payments-silver")
                .withPieces(List.of(piece("lake", HealthStatus.DEGRADED), piece("bus", HealthStatus.UNHEALTHY)));

        assertThat(payload.status()).isEqualTo(HealthStatus.UNHEALTHY);
    }

    @Test
    void withPieces_doesNotMakeAnUnhealthyModuleLookHealthier() {
        ModuleHealthPayload payload = ModuleHealthPayload.unhealthy("payments-silver", List.of(HealthCheck.unhealthy("source", "down")))
                .withPieces(List.of(piece("lake", HealthStatus.HEALTHY)));

        assertThat(payload.status()).isEqualTo(HealthStatus.UNHEALTHY);
    }

    @Test
    void factories_startWithoutPieces() {
        assertThat(ModuleHealthPayload.healthy("payments-silver").pieces()).isEmpty();
        assertThat(ModuleHealthPayload.of("payments-silver", List.of()).pieces()).isEmpty();
    }

    private static PieceHealth piece(String name, HealthStatus status) {
        return new PieceHealth(name, "FILE_SYSTEM", status, Optional.empty(), NOW);
    }
}
