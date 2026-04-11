package software.spool.core.port.logging;

public interface Logger {
    void debug(String message);
    void debug(String format, Object... args);
    void info(String message);
    void info(String format, Object... args);
    void warn(String message);
    void warn(String format, Object... args);
    void warn(String message, Throwable cause);
    void error(String message);
    void error(String format, Object... args);
    void error(String message, Throwable cause);

    Logger NOOP = new Logger() {
        public void debug(String m) {}
        public void debug(String f, Object... a) {}
        public void info(String m) {}
        public void info(String f, Object... a) {}
        public void warn(String m) {}
        public void warn(String f, Object... a) {}
        public void warn(String m, Throwable t) {}
        public void error(String m) {}
        public void error(String f, Object... a) {}
        public void error(String m, Throwable t) {}
    };
}
