package software.spool.core.port.serde;

@FunctionalInterface
public interface PayloadMapper<I, P> {
    P map(I input);
}