package software.spool.core.model.vo;

import org.junit.jupiter.api.Test;
import software.spool.core.exception.IdempotencyKeyException;
import software.spool.core.model.vo.IdempotencyKeySchema.WhenFieldIsMissing;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IdempotencyKeySchemaTest {
    private static final String SOURCE = "boe";
    private static final IdempotencyKeySchema BY_ID = IdempotencyKeySchema.of(List.of("id"));

    @Test
    void keyOf_recordsThatDifferOnlyOutsideTheSchema_haveTheSameKey() {
        assertThat(BY_ID.keyOf(SOURCE, json("{\"id\":\"a-1\",\"fetchedAt\":\"10:01\"}")))
                .isEqualTo(BY_ID.keyOf(SOURCE, json("{\"id\":\"a-1\",\"fetchedAt\":\"10:02\"}")));
    }

    @Test
    void keyOf_recordsThatDifferInAFieldOfTheSchema_haveDifferentKeys() {
        IdempotencyKeySchema byIdAndVersion = IdempotencyKeySchema.of(List.of("id", "version"));

        assertThat(byIdAndVersion.keyOf(SOURCE, json("{\"id\":\"a-1\",\"version\":1}")))
                .isNotEqualTo(byIdAndVersion.keyOf(SOURCE, json("{\"id\":\"a-1\",\"version\":2}")));
    }

    @Test
    void keyOf_sameFieldsInAnotherOrder_haveTheSameKey() {
        IdempotencyKeySchema byIdAndVersion = IdempotencyKeySchema.of(List.of("id", "version"));

        assertThat(byIdAndVersion.keyOf(SOURCE, json("{\"id\":\"a-1\",\"version\":1}")))
                .isEqualTo(byIdAndVersion.keyOf(SOURCE, json("{\"version\":1,\"id\":\"a-1\"}")));
    }

    @Test
    void keyOf_sameFieldsOfAnotherSource_haveDifferentKeys() {
        byte[] record = json("{\"id\":\"a-1\"}");

        assertThat(BY_ID.keyOf(SOURCE, record)).isNotEqualTo(BY_ID.keyOf("sec", record));
    }

    @Test
    void keyOf_fieldInsideAnObject_identifiesTheRecord() {
        IdempotencyKeySchema byMetaId = IdempotencyKeySchema.of(List.of("meta.id"));

        assertThat(byMetaId.keyOf(SOURCE, json("{\"meta\":{\"id\":\"a-1\"},\"fetchedAt\":\"10:01\"}")))
                .isEqualTo(byMetaId.keyOf(SOURCE, json("{\"meta\":{\"id\":\"a-1\"},\"fetchedAt\":\"10:02\"}")));
    }

    @Test
    void keyOf_missingFieldWhenTheSchemaFails_isRejectedNamingTheField() {
        assertThatThrownBy(() -> BY_ID.keyOf(SOURCE, json("{\"title\":\"Ley 1/2026\"}")))
                .isInstanceOf(IdempotencyKeyException.class)
                .hasMessageContaining("id")
                .hasMessageContaining(SOURCE);
    }

    @Test
    void keyOf_recordThatIsNotAJsonObjectWhenTheSchemaFails_isRejected() {
        assertThatThrownBy(() -> BY_ID.keyOf(SOURCE, json("Ley 1/2026")))
                .isInstanceOf(IdempotencyKeyException.class);
    }

    @Test
    void keyOf_missingFieldWhenTheSchemaUsesTheWholeRecord_isTheKeyOfTheWholeRecord() {
        IdempotencyKeySchema schema = new IdempotencyKeySchema(List.of("id"), WhenFieldIsMissing.WHOLE_RECORD);
        byte[] record = json("{\"title\":\"Ley 1/2026\"}");

        assertThat(schema.keyOf(SOURCE, record)).isEqualTo(IdempotencyKey.of(SOURCE, record));
    }

    @Test
    void keyOf_wholeRecord_isTheKeyOfTheWholeRecord() {
        byte[] record = json("{\"id\":\"a-1\",\"fetchedAt\":\"10:01\"}");

        assertThat(IdempotencyKeySchema.wholeRecord().keyOf(SOURCE, record)).isEqualTo(IdempotencyKey.of(SOURCE, record));
    }

    private static byte[] json(String content) {
        return content.getBytes(StandardCharsets.UTF_8);
    }
}
