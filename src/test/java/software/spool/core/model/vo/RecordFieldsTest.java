package software.spool.core.model.vo;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class RecordFieldsTest {
    private static final RecordFields RECORD = RecordFields.of(json("{\"id\":\"a-1\",\"meta\":{\"version\":2},\"note\":null}")).orElseThrow();

    @Test
    void find_fieldAtTheTop_returnsItsValue() {
        assertThat(RECORD.find("id")).contains("a-1");
    }

    @Test
    void find_fieldInsideAnObject_returnsItsValue() {
        assertThat(RECORD.find("meta.version")).contains(2);
    }

    @Test
    void find_missingField_returnsNothing() {
        assertThat(RECORD.find("meta.author")).isEmpty();
    }

    @Test
    void find_pathThroughAValueThatIsNotAnObject_returnsNothing() {
        assertThat(RECORD.find("id.value")).isEmpty();
    }

    @Test
    void find_fieldHoldingNull_returnsNothing() {
        assertThat(RECORD.find("note")).isEmpty();
    }

    @Test
    void of_recordThatIsNotAJsonObject_hasNoFields() {
        assertThat(RecordFields.of(json("Ley 1/2026"))).isEmpty();
        assertThat(RecordFields.of(json("[1, 2]"))).isEmpty();
    }

    private static byte[] json(String content) {
        return content.getBytes(StandardCharsets.UTF_8);
    }
}
