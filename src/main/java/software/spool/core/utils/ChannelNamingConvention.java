package software.spool.core.utils;

import software.spool.core.model.Event;

/**
 * Naming strategies for deriving default channel names from event class names.
 *
 * <p>
 * Used by {@link ChannelRouter} when no explicit route is registered for
 * an event type. For example, {@code OrderReceived} becomes:
 * </p>
 * <ul>
 * <li>{@link #DOT_CASE} — {@code order.received}</li>
 * <li>{@link #SNAKE_CASE} — {@code order_received}</li>
 * <li>{@link #KEBAB_CASE} — {@code order-received}</li>
 * </ul>
 */
public enum ChannelNamingConvention {

    DOT_CASE {
        @Override
        public String apply(Class<? extends Event> type) {
            return splitCamelCase(type.getSimpleName(), ".");
        }
    },
    SNAKE_CASE {
        @Override
        public String apply(Class<? extends Event> type) {
            return splitCamelCase(type.getSimpleName(), "_");
        }
    },
    KEBAB_CASE {
        @Override
        public String apply(Class<? extends Event> type) {
            return splitCamelCase(type.getSimpleName(), "-");
        }
    };

    public abstract String apply(Class<? extends Event> type);

    private static String splitCamelCase(String name, String separator) {
        return name
                .replaceAll("([a-z])([A-Z])", "$1" + separator + "$2")
                .toLowerCase();
    }
}
