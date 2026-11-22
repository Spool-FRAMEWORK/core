package software.spool.core.port.watchdog;

import org.junit.jupiter.api.Test;
import software.spool.core.model.watchdog.ModuleIdentity;

import static org.assertj.core.api.Assertions.assertThat;

class ModuleHeartBeatTest {
    @Test
    void silent_keepsTheIdOfItsModule() {
        assertThat(ModuleHeartBeat.silent(ModuleIdentity.of("payments-silver")).identity().moduleId()).isEqualTo("payments-silver");
    }
}
