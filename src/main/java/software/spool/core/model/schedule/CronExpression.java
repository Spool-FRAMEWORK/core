package software.spool.core.model.schedule;

import software.spool.core.port.schedule.CronCalendar;

import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * A Unix cron expression of 5 fields (minute, hour, day of the month, month, day of the week) or a macro such as
 * {@code @daily}. It only exists when its calendar accepts it, and that calendar gives its dates.
 */
public final class CronExpression {
    private final String text;
    private final CronCalendar calendar;

    private CronExpression(String text, CronCalendar calendar) {
        this.text = text;
        this.calendar = calendar;
    }

    public static CronExpression parse(String text, CronCalendar calendar) {
        Objects.requireNonNull(calendar, "calendar");
        if (text == null || text.isBlank()) throw new InvalidScheduleException("Cron expression required");
        calendar.problemWith(text).ifPresent(problem -> {
            throw new InvalidScheduleException("Invalid cron expression '" + text + "': " + problem);
        });
        return new CronExpression(text, calendar);
    }

    public String text() {
        return text;
    }

    /** The first date of the expression strictly after the moment, in the zone of the moment. */
    public ZonedDateTime nextExecutionAfter(ZonedDateTime moment) {
        return calendar.nextExecutionAfter(text, moment);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof CronExpression expression && expression.text.equals(text);
    }

    @Override
    public int hashCode() {
        return text.hashCode();
    }

    @Override
    public String toString() {
        return text;
    }
}
