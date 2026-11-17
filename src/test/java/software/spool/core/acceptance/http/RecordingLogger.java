package software.spool.core.acceptance.http;

import software.spool.core.port.logging.Logger;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** Keeps every line logged, with its arguments put in place of the {@code {}} markers. */
final class RecordingLogger implements Logger {
    private final List<String> lines = new CopyOnWriteArrayList<>();

    List<String> lines() {
        return List.copyOf(lines);
    }

    private void record(String format, Object... args) {
        StringBuilder line = new StringBuilder();
        int next = 0;
        int argument = 0;
        for (int marker = format.indexOf("{}"); marker >= 0; marker = format.indexOf("{}", next)) {
            line.append(format, next, marker).append(argument < args.length ? args[argument++] : "{}");
            next = marker + 2;
        }
        lines.add(line.append(format.substring(next)).toString());
    }

    @Override public void debug(String message) { record(message); }
    @Override public void debug(String format, Object... args) { record(format, args); }
    @Override public void info(String message) { record(message); }
    @Override public void info(String format, Object... args) { record(format, args); }
    @Override public void warn(String message) { record(message); }
    @Override public void warn(String format, Object... args) { record(format, args); }
    @Override public void warn(String message, Throwable cause) { record(message + " " + cause.getMessage()); }
    @Override public void error(String message) { record(message); }
    @Override public void error(String format, Object... args) { record(format, args); }
    @Override public void error(String message, Throwable cause) { record(message + " " + cause.getMessage()); }
}
