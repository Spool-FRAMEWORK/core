package software.spool.core.spi.factory;

public interface StructuredFormatProvider {
    String getName();
    StructuredPayloadDeserializerBuilder builder();
}