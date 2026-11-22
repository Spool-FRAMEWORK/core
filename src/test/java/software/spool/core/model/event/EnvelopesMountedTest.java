package software.spool.core.model.event;

import org.junit.jupiter.api.Test;
import software.spool.core.adapter.jackson.PayloadDeserializerFactory;
import software.spool.core.adapter.jackson.RecordSerializerFactory;
import software.spool.core.model.vo.PartitionKey;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EnvelopesMountedTest {

    private static final PartitionKey PARTITION = new PartitionKey("source=payments::country=ES");

    @Test
    void roundTrip_keepsTheDataMartAndThePartition() {
        EnvelopesMounted event = EnvelopesMounted.builder().dataMart("silver/payments").partitionKey(PARTITION).build();

        EnvelopesMounted read = PayloadDeserializerFactory.json().as(EnvelopesMounted.class)
                .deserialize(RecordSerializerFactory.record().serialize(event));

        assertThat(read).isEqualTo(event);
    }

    @Test
    void build_withoutDataMart_isRejected() {
        assertThatThrownBy(() -> EnvelopesMounted.builder().partitionKey(PARTITION).build())
                .isInstanceOf(NullPointerException.class)
                .hasMessage("dataMart is required");
    }
}
