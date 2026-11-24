package software.spool.core.model.spool;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * The labels of a node, such as {@code cloud=aws} or {@code region=eu-west-1}, or the ones a deployment asks of the
 * node it runs on (ADR-036). Each label is written as {@code key=value}, both parts not empty, and a key has one value.
 */
public final class NodeLabels {
    private static final String KEY_VALUE_SEPARATOR = "=";
    private static final String LABEL_SEPARATOR = ",";
    private static final NodeLabels NONE = new NodeLabels(Map.of());

    private final Map<String, String> byKey;

    private NodeLabels(Map<String, String> byKey) {
        this.byKey = Map.copyOf(byKey);
    }

    public static NodeLabels none() {
        return NONE;
    }

    /**
     * The labels, each written as {@code key=value}.
     *
     * @throws IllegalArgumentException if a label is not written as {@code key=value}, or a key has two values
     */
    public static NodeLabels of(Collection<String> labels) {
        Map<String, String> byKey = new TreeMap<>();
        for (String label : labels) {
            String[] keyAndValue = keyAndValueOf(label);
            String previous = byKey.putIfAbsent(keyAndValue[0], keyAndValue[1]);
            if (previous != null && !previous.equals(keyAndValue[1])) {
                throw new IllegalArgumentException("The label " + keyAndValue[0] + " has two values, " + previous
                        + " and " + keyAndValue[1]);
            }
        }
        return byKey.isEmpty() ? NONE : new NodeLabels(byKey);
    }

    /**
     * The labels written as {@code key=value} and separated by commas, as in an environment variable. A blank text
     * has no labels.
     *
     * @throws IllegalArgumentException if a label is not written as {@code key=value}, or a key has two values
     */
    public static NodeLabels parse(String labels) {
        Objects.requireNonNull(labels, "labels are required");
        if (labels.isBlank()) return NONE;
        return of(Arrays.stream(labels.split(LABEL_SEPARATOR)).map(String::trim).toList());
    }

    /** Whether these labels have every one of {@code asked}, with the same value. */
    public boolean include(NodeLabels asked) {
        return byKey.entrySet().containsAll(asked.byKey.entrySet());
    }

    /** Each label written as {@code key=value}, in the order of their keys. */
    public List<String> asList() {
        return new TreeMap<>(byKey).entrySet().stream()
                .map(label -> label.getKey() + KEY_VALUE_SEPARATOR + label.getValue())
                .toList();
    }

    public boolean isEmpty() {
        return byKey.isEmpty();
    }

    private static String[] keyAndValueOf(String label) {
        String[] keyAndValue = Objects.requireNonNull(label, "a label is required").split(KEY_VALUE_SEPARATOR, -1);
        if (keyAndValue.length != 2 || keyAndValue[0].isBlank() || keyAndValue[1].isBlank()
                || label.contains(LABEL_SEPARATOR) || !label.equals(label.strip())) {
            throw new IllegalArgumentException("The label \"" + label + "\" must be written as key=value, "
                    + "with neither part empty");
        }
        return keyAndValue;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof NodeLabels labels && byKey.equals(labels.byKey);
    }

    @Override
    public int hashCode() {
        return byKey.hashCode();
    }

    @Override
    public String toString() {
        return String.join(LABEL_SEPARATOR, asList());
    }
}
