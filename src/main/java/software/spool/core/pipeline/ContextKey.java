package software.spool.core.pipeline;

public final class ContextKey<T> {
    private final String name;

    private ContextKey(String name) { this.name = name; }

    public static <T> ContextKey<T> of(String name) {
        return new ContextKey<>(name);
    }

    @Override
    public String toString() { return name; }
}