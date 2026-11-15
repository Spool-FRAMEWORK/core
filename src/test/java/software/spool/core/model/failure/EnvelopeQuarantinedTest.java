package software.spool.core.model.failure;

import org.junit.jupiter.api.Test;
import software.spool.core.adapter.jackson.PayloadDeserializerFactory;
import software.spool.core.adapter.jackson.RecordSerializerFactory;
import software.spool.core.model.quarantine.QuarantineLocation;
import software.spool.core.model.quarantine.QuarantineReason;
import software.spool.core.model.vo.PartitionKey;
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
    void build_withoutLocation_saysTheEnvelopeStayedInTheInbox() {
        EnvelopeQuarantined event = EnvelopeQuarantined.builder()
                .idempotencyKey(IdempotencyKey.of("k1"))
                .reason(QuarantineReason.VALIDATION_FAILED)
                .build();

        assertThat(event.location()).isEqualTo(QuarantineLocation.inbox());
    }

    @Test
    void build_withABronzeLocation_keepsItsPartition() {
        PartitionKey partition = PartitionKey.ofEntriesWithoutDate().with("source", "boe").build();

        EnvelopeQuarantined event = EnvelopeQuarantined.builder()
                .idempotencyKey(IdempotencyKey.of("k1"))
                .reason(QuarantineReason.VALIDATION_FAILED)
                .location(QuarantineLocation.bronze(partition))
                .build();

        assertThat(event.location()).isEqualTo(QuarantineLocation.bronze(partition));
    }

    @Test
    void roundTrip_bronzeLocation_isKeptInJson() {
        PartitionKey partition = PartitionKey.ofEntriesWithoutDate().with("source", "boe").build();
        EnvelopeQuarantined event = EnvelopeQuarantined.builder()
                .idempotencyKey(IdempotencyKey.of("k1"))
                .reason(QuarantineReason.VALIDATION_FAILED)
                .location(QuarantineLocation.bronze(partition))
                .build();

        EnvelopeQuarantined read = PayloadDeserializerFactory.json().as(EnvelopeQuarantined.class)
                .deserialize(RecordSerializerFactory.record().serialize(event));

        assertThat(read.location()).isEqualTo(QuarantineLocation.bronze(partition));
    }

    @Test
    void build_withoutReason_fails() {
        assertThatThrownBy(() -> EnvelopeQuarantined.builder().idempotencyKey(IdempotencyKey.of("k1")).build())
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("reason");
    }
}
