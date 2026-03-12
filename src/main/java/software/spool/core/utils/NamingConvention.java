package software.spool.core.utils;

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
    KEBAB_CASE
}
