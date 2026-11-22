package software.spool.core.adapter.schedule;

import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import software.spool.core.port.schedule.CronCalendar;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * The cron calendar of cron-utils, with its Unix definition. That definition reads no macros, so each macro is
 * read as the 5 fields it stands for. {@code @reboot} is not a date, so it is rejected.
 */
public class CronUtilsCronCalendar implements CronCalendar {
    private static final String REBOOT = "@reboot";
    private static final Map<String, String> FIELDS_BY_MACRO = Map.of(
            "@hourly", "0 * * * *",
            "@daily", "0 0 * * *",
            "@midnight", "0 0 * * *",
            "@weekly", "0 0 * * 0",
            "@monthly", "0 0 1 * *",
            "@yearly", "0 0 1 1 *",
            "@annually", "0 0 1 1 *");

    private final CronParser parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX));
    private final Clock clock;

    public CronUtilsCronCalendar() {
        this(Clock.systemUTC());
    }

    public CronUtilsCronCalendar(Clock clock) {
        this.clock = clock;
    }

    @Override
    public Optional<String> problemWith(String expression) {
        if (normalized(expression).equals(REBOOT)) return Optional.of(REBOOT + " is not a date");
        try {
            return nextExecution(expression, ZonedDateTime.now(clock)).isPresent()
                    ? Optional.empty()
                    : Optional.of("matches no date");
        } catch (IllegalArgumentException unreadable) {
            return Optional.of(unreadable.getMessage());
        }
    }

    @Override
    public ZonedDateTime nextExecutionAfter(String expression, ZonedDateTime moment) {
        return nextExecution(expression, moment).orElseThrow(() ->
                new IllegalStateException("Cron expression '" + expression + "' has no date after " + moment));
    }

    private Optional<ZonedDateTime> nextExecution(String expression, ZonedDateTime moment) {
        Cron cron = parser.parse(FIELDS_BY_MACRO.getOrDefault(normalized(expression), expression));
        return ExecutionTime.forCron(cron).nextExecution(moment);
    }

    private static String normalized(String expression) {
        return expression.strip().toLowerCase(Locale.ROOT);
    }
}
