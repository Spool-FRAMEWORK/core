package software.spool.core.port.serde;

public interface PayloadLocator<P> {
    P locate(P payload);
}
