package software.spool.core.utils;

import software.spool.core.model.Event;

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
