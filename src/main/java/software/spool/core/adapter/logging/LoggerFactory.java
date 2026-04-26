package software.spool.core.adapter.logging;

import software.spool.core.port.logging.Logger;

public final class LoggerFactory {
    private LoggerFactory() {}

    public static Logger getLogger(Class<?> clazz) {
        return new Slf4Logger(clazz);
    }

    public static Logger getLogger(String name) {
        return new Slf4Logger(name);
    }
}