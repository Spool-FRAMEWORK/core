package software.spool.core.utils.time;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class ReadableDurationTest {

    @ParameterizedTest
    @CsvSource({"100ms, PT0.1S", "5s, PT5S", "10m, PT10M", "2h, PT2H", "30d, PT720H", "PT1H, PT1H", "P30D, PT720H"})
    void parse_readsTheShortFormAndIso8601(String written, String expected) {
        assertThat(ReadableDuration.parse(written)).contains(Duration.parse(expected));
    }

    @ParameterizedTest
    @ValueSource(strings = {"30", "five seconds", "-5s", "PT-5S", ""})
    void parse_rejectsWhatIsNotADuration(String written) {
        assertThat(ReadableDuration.parse(written)).isEmpty();
    }

    @Test
    void parse_rejectsWhatIsNotText() {
        assertThat(ReadableDuration.parse(30)).isEmpty();
    }
}
