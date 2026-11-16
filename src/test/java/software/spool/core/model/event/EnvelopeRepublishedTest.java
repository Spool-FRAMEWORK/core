package software.spool.core.model.event;

import org.junit.jupiter.api.Test;
import software.spool.core.adapter.jackson.PayloadDeserializerFactory;
import software.spool.core.adapter.jackson.RecordSerializerFactory;
import software.spool.core.model.vo.IdempotencyKey;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class EnvelopeRepublishedTest {

    private static final IdempotencyKey KEY = IdempotencyKey.of("k1");

    @Test
    void build_bypassed_saysItWasBypassed() {
        EnvelopeRepublished event = EnvelopeRepublished.builder()
                .idempotencyKey(KEY)
                .republishedIdempotencyKey(KEY)
                .resolution(EnvelopeRepublished.Resolution.BYPASSED)
                .build();

        assertThat(event.resolution()).isEqualTo(EnvelopeRepublished.Resolution.BYPASSED);
        assertThat(event.wasCorrected()).isFalse();
    }

    @Test
    void build_withoutResolutionAndTheSameKey_isUnchanged() {
        EnvelopeRepublished event = EnvelopeRepublished.builder()
                .idempotencyKey(KEY)
                .republishedIdempotencyKey(KEY)
                .build();

        assertThat(event.resolution()).isEqualTo(EnvelopeRepublished.Resolution.UNCHANGED);
    }

    @Test
    void build_withoutResolutionAndANewKey_isCorrected() {
        EnvelopeRepublished event = EnvelopeRepublished.builder()
                .idempotencyKey(KEY)
                .republishedIdempotencyKey(IdempotencyKey.of("k2"))
                .build();

        assertThat(event.resolution()).isEqualTo(EnvelopeRepublished.Resolution.CORRECTED);
        assertThat(event.wasCorrected()).isTrue();
    }

    @Test
    void roundTrip_bypassed_keepsItsResolution() {
        EnvelopeRepublished event = EnvelopeRepublished.builder()
                .idempotencyKey(KEY)
                .republishedIdempotencyKey(KEY)
                .resolution(EnvelopeRepublished.Resolution.BYPASSED)
                .build();

        EnvelopeRepublished read = PayloadDeserializerFactory.json().as(EnvelopeRepublished.class)
                .deserialize(RecordSerializerFactory.record().serialize(event));

        assertThat(read.resolution()).isEqualTo(EnvelopeRepublished.Resolution.BYPASSED);
    }

    @Test
    void deserialize_eventPublishedBeforeTheResolutionExisted_deducesItFromTheKeys() {
        EnvelopeRepublished corrected = EnvelopeRepublished.builder()
                .idempotencyKey(KEY)
                .republishedIdempotencyKey(IdempotencyKey.of("k2"))
                .build();
        Map<String, Object> withoutResolution = new HashMap<>(PayloadDeserializerFactory.json().asMap()
                .deserialize(RecordSerializerFactory.record().serialize(corrected)));
        withoutResolution.remove("resolution");

        EnvelopeRepublished read = PayloadDeserializerFactory.json().as(EnvelopeRepublished.class)
                .deserialize(RecordSerializerFactory.record().serialize(withoutResolution));

        assertThat(read.resolution()).isEqualTo(EnvelopeRepublished.Resolution.CORRECTED);
    }
}
