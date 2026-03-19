package software.spool.core.port;

public interface TraceScope extends AutoCloseable {
    void error(Exception error);
    void end();
    @Override
    default void close() {
        end();
    }
}
