package software.spool.core.adapter.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Slf4Logger implements software.spool.core.port.logging.Logger {
    private final Logger log;

    public Slf4Logger(Class<?> clazz) {
        this.log = LoggerFactory.getLogger(clazz);
    }

    public Slf4Logger(String name) {
        this.log = LoggerFactory.getLogger(name);
    }

    @Override public void debug(String m) { log.debug(m); }
    @Override public void debug(String f, Object... a) { log.debug(f, a); }
    @Override public void info(String m) { log.info(m); }
    @Override public void info(String f, Object... a) { log.info(f, a); }
    @Override public void warn(String m) { log.warn(m); }
    @Override public void warn(String f, Object... a) { log.warn(f, a); }
    @Override public void warn(String m, Throwable t) { log.warn(m, t); }
    @Override public void error(String m) { log.error(m); }
    @Override public void error(String f, Object... a) { log.error(f, a); }
    @Override public void error(String m, Throwable t) { log.error(m, t); }
}