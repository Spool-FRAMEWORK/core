package software.spool.core.adapter.schedule;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class CronUtilsCronCalendarTest {
    private static final ZonedDateTime SUNDAY_MORNING = ZonedDateTime.parse("2026-11-22T10:07:00Z");

    private final CronUtilsCronCalendar calendar = new CronUtilsCronCalendar();

    @ParameterizedTest
    @CsvSource({
            "@hourly,   2026-11-22T11:00:00Z",
            "@daily,    2026-11-23T00:00:00Z",
            "@midnight, 2026-11-23T00:00:00Z",
            "@weekly,   2026-11-29T00:00:00Z",
            "@monthly,  2026-12-01T00:00:00Z",
            "@yearly,   2027-01-01T00:00:00Z",
            "@annually, 2027-01-01T00:00:00Z"
    })
    void macroIsAcceptedAndGivesItsNextDate(String macro, String next) {
        assertThat(calendar.problemWith(macro)).isEmpty();
        assertThat(calendar.nextExecutionAfter(macro, SUNDAY_MORNING).toInstant())
                .isEqualTo(ZonedDateTime.parse(next).toInstant());
    }

    @ParameterizedTest
    @ValueSource(strings = {"@reboot", "@REBOOT"})
    void rebootIsRejectedBecauseItIsNotADate(String reboot) {
        assertThat(calendar.problemWith(reboot)).contains("@reboot is not a date");
    }

    @Test
    void expressionThatMatchesNoDateIsRejected() {
        assertThat(calendar.problemWith("0 0 30 2 *")).contains("matches no date");
    }

    @Test
    void namesOfMonthsAndDaysAreAccepted() {
        assertThat(calendar.problemWith("0 2 * JAN,APR,JUL,OCT MON-FRI")).isEmpty();
    }
}
