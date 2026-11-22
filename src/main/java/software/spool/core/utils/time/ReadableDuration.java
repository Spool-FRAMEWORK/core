package software.spool.core.utils.time;

import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A duration as a person writes it in a descriptor: ISO 8601 ({@code PT5S}, {@code P30D}) or the short form
 * ({@code 100ms}, {@code 5s}, {@code 10m}, {@code 2h}, {@code 30d}), as ADR-011 decides. A bare number is not a
 * duration, because nothing would say its unit.
 */
public final class ReadableDuration {
    private static final Pattern SHORT_FORM = Pattern.compile("(\\d+)(ms|s|m|h|d)");
    private static final Map<String, ChronoUnit> UNITS = Map.of(
            "d", ChronoUnit.DAYS, "h", ChronoUnit.HOURS, "m", ChronoUnit.MINUTES,
            "s", ChronoUnit.SECONDS, "ms", ChronoUnit.MILLIS);

    private ReadableDuration() {
    }

    public static Optional<Duration> parse(Object raw) {
        if (!(raw instanceof String text)) return Optional.empty();
        return shortForm(text.strip()).or(() -> iso(text.strip())).filter(duration -> !duration.isNegative());
    }

    private static Optional<Duration> shortForm(String text) {
        Matcher matcher = SHORT_FORM.matcher(text);
        if (!matcher.matches()) return Optional.empty();
        try {
            return Optional.of(Duration.of(Long.parseLong(matcher.group(1)), UNITS.get(matcher.group(2))));
        } catch (NumberFormatException | ArithmeticException tooLarge) {
            return Optional.empty();
        }
    }

    private static Optional<Duration> iso(String text) {
        try {
            return Optional.of(Duration.parse(text));
        } catch (DateTimeParseException notIso) {
            return Optional.empty();
        }
    }
}
