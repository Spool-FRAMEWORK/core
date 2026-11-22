package software.spool.core.model.watchdog;

import org.junit.jupiter.api.Test;
import software.spool.core.adapter.jackson.PayloadDeserializerFactory;
import software.spool.core.adapter.jackson.RecordSerializerFactory;
import software.spool.core.port.health.HealthStatus;
import software.spool.core.port.health.PieceHealth;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class ModuleStateJsonTest {
    private static final Instant NOW = Instant.parse("2026-11-22T10:00:00Z");

    @Test
    void stateWithItsPieces_isReadBackAsItWasWritten() {
        ModuleState state = new ModuleState(ModuleIdentity.of("payments-silver"), NOW, ModuleStatus.UNHEALTHY,
                List.of(new PieceHealth("lake", "S3", HealthStatus.UNHEALTHY, Optional.of("the bucket does not exist"), NOW, true),
                        new PieceHealth("bus", "IN_MEMORY", HealthStatus.HEALTHY, Optional.empty(), NOW, false)));

        ModuleState read = PayloadDeserializerFactory.json().as(ModuleState.class)
                .deserialize(RecordSerializerFactory.record().serialize(state));

        assertThat(read).isEqualTo(state);
    }

    @Test
    void heartbeatFromAModuleThatDoesNotReportItsPieces_hasNone() {
        HeartbeatPayload payload = PayloadDeserializerFactory.json().as(HeartbeatPayload.class)
                .deserialize("{\"identity\":{\"moduleId\":\"payments-silver\"},\"status\":\"HEALTHY\"}".getBytes());

        assertThat(payload.pieces()).isEmpty();
    }
}
