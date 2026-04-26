package software.spool.core.adapter.jackson;

import software.spool.core.spi.factory.StructuredFormatProvider;
import software.spool.core.spi.factory.StructuredPayloadDeserializerBuilder;

public final class JsonStructuredFormatProvider implements StructuredFormatProvider {
    @Override
    public String getName() {
        return "JSON";
    }

    @Override
    public StructuredPayloadDeserializerBuilder builder() {
        return new JacksonStructuredPayloadDeserializerBuilder(JacksonMapperFactory.json());
    }
}