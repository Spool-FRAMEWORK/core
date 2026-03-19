package software.spool.core.utils;

import software.spool.core.infrastructure.adapter.DomainMapperFactory;
import software.spool.core.port.PayloadDeserializer;

/**
 * JSON property naming strategies used when deserializing domain events.
 *
 * <p>
 * Controls how JSON field names are mapped to Java record/class fields
 * during domain event deserialization.
 * </p>
 */
public enum NamingConvention {
    CAMEL_CASE,
    SNAKE_CASE,
    PASCAL_CASE,
    KEBAB_CASE;

    public <D> PayloadDeserializer<D> deserializerFor(Class<D> type) {
        return switch (this) {
            case SNAKE_CASE  -> DomainMapperFactory.snakeCase(type);
            case CAMEL_CASE  -> DomainMapperFactory.camelCase(type);
            case PASCAL_CASE -> DomainMapperFactory.pascalCase(type);
            case KEBAB_CASE  -> DomainMapperFactory.kebabCase(type);
        };
    }
}
