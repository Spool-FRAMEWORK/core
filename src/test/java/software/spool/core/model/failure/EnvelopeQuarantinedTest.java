package software.spool.core.model.failure;

import org.junit.jupiter.api.Test;
import software.spool.core.model.quarantine.QuarantineReason;
import software.spool.core.model.vo.IdempotencyKey;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EnvelopeQuarantinedTest {

    @Test
    void build_withReasonAndViolations_keepsBoth() {
        EnvelopeQuarantined event = EnvelopeQuarantined.builder()
                .idempotencyKey(IdempotencyKey.of("k1"))
                .reason(QuarantineReason.VALIDATION_FAILED)
                .violations(List.of("price must be positive"))
                .build();

        assertThat(event.reason()).isEqualTo(QuarantineReason.VALIDATION_FAILED);
        assertThat(event.violations()).containsExactly("price must be positive");
    }

    @Test
    void build_withoutViolations_hasAnEmptyList() {
        EnvelopeQuarantined event = EnvelopeQuarantined.builder()
                .idempotencyKey(IdempotencyKey.of("k1"))
                .reason(QuarantineReason.RETRIES_EXHAUSTED)
                .build();

        assertThat(event.violations()).isEmpty();
    }

    @Test
    void build_withoutReason_fails() {
        assertThatThrownBy(() -> EnvelopeQuarantined.builder().idempotencyKey(IdempotencyKey.of("k1")).build())
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("reason");
    }
}
