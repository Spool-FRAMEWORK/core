package software.spool.core.adapter.jackson;

import software.spool.core.spi.factory.StructuredFormatProvider;
import software.spool.core.spi.factory.StructuredPayloadDeserializerBuilder;

public final class YamlStructuredFormatProvider implements StructuredFormatProvider {

    @Override
    public String getName() {
        return "YAML";
    }

    @Override
    public StructuredPayloadDeserializerBuilder builder() {
        return new JacksonStructuredPayloadDeserializerBuilder(JacksonMapperFactory.yaml());
    }
}