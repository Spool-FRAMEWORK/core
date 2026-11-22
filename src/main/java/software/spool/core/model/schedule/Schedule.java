package software.spool.core.model.schedule;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

/**
 * When a module that works in rounds runs (ADR-015): once, every interval or on the dates of a cron expression in
 * a time zone. Running once and every interval start when the module starts; a cron expression waits for its
 * first date. The next execution comes after the round ends, so the dates that pass during a round are skipped.
 */
public sealed interface Schedule permits Schedule.Once, Schedule.Every, Schedule.Cron {

    ZonedDateTime firstExecution(ZonedDateTime start);

    /** The execution that follows a round, or nothing when the schedule has no more. */
    Optional<ZonedDateTime> nextExecutionAfter(ZonedDateTime roundEnd);

    static Schedule once() {
        return new Once();
    }

    static Schedule every(Duration interval) {
        return new Every(interval);
    }

    static Schedule cron(CronExpression expression, ZoneId zone) {
        return new Cron(expression, zone);
    }

    record Once() implements Schedule {
        @Override
        public ZonedDateTime firstExecution(ZonedDateTime start) {
            return start;
        }

        @Override
        public Optional<ZonedDateTime> nextExecutionAfter(ZonedDateTime roundEnd) {
            return Optional.empty();
        }
    }

    record Every(Duration interval) implements Schedule {
        public Every {
            if (interval == null) throw new InvalidScheduleException("Schedule interval required");
            if (interval.isNegative() || interval.isZero()) {
                throw new InvalidScheduleException("Schedule interval must be positive: " + interval);
            }
        }

        @Override
        public ZonedDateTime firstExecution(ZonedDateTime start) {
            return start;
        }

        @Override
        public Optional<ZonedDateTime> nextExecutionAfter(ZonedDateTime roundEnd) {
            return Optional.of(roundEnd.plus(interval));
        }
    }

    record Cron(CronExpression expression, ZoneId zone) implements Schedule {
        public Cron {
            if (expression == null) throw new InvalidScheduleException("Cron schedule without expression");
            if (zone == null) throw new InvalidScheduleException("Cron schedule without time zone: " + expression);
        }

        @Override
        public ZonedDateTime firstExecution(ZonedDateTime start) {
            return expression.nextExecutionAfter(start.withZoneSameInstant(zone));
        }

        @Override
        public Optional<ZonedDateTime> nextExecutionAfter(ZonedDateTime roundEnd) {
            return Optional.of(expression.nextExecutionAfter(roundEnd.withZoneSameInstant(zone)));
        }
    }
}
