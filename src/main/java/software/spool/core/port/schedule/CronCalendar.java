package software.spool.core.port.schedule;

import java.time.ZonedDateTime;
import java.util.Optional;

/** Reads Unix cron expressions of 5 fields and macros, and finds their dates in a time zone. */
public interface CronCalendar {

    /** Why the expression is not valid, or nothing when it is. */
    Optional<String> problemWith(String expression);

    /** The first date of a valid expression strictly after the moment, in the zone of the moment. */
    ZonedDateTime nextExecutionAfter(String expression, ZonedDateTime moment);
}
