package software.spool.core.model.vo;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Composes and separates keys made of {@code name=value} entries joined by {@code ::}.
 *
 * <p>
 * Values are escaped so that {@code ::}, {@code =} and {@code \} inside them never split
 * a key: {@link #parse(String)} returns exactly the values given to {@link #with(String, Object)}.
 * Names are written as they are and must not contain those characters.
 * </p>
 */
public final class KeyEntries {
    private static final String ENTRY_SEPARATOR = "::";
    private static final char NAME_VALUE_SEPARATOR = '=';
    private static final char ESCAPE = '\\';
    private static final char COLON = ':';

    private final Map<String, Object> entries = new LinkedHashMap<>();

    public KeyEntries with(String name, Object value) {
        entries.put(name, value);
        return this;
    }

    public String format() {
        return entries.entrySet().stream()
                .map(entry -> entry.getKey() + NAME_VALUE_SEPARATOR + escape(String.valueOf(entry.getValue())))
                .collect(Collectors.joining(ENTRY_SEPARATOR));
    }

    public static String join(List<String> formattedEntries) {
        return String.join(ENTRY_SEPARATOR, formattedEntries);
    }

    public static List<String> split(String key) {
        List<String> formattedEntries = new ArrayList<>();
        if (key.isEmpty()) return formattedEntries;
        StringBuilder current = new StringBuilder();
        for (int position = 0; position < key.length(); position++) {
            char character = key.charAt(position);
            if (character == ESCAPE && position + 1 < key.length()) {
                current.append(character).append(key.charAt(++position));
            } else if (key.startsWith(ENTRY_SEPARATOR, position)) {
                formattedEntries.add(current.toString());
                current.setLength(0);
                position += ENTRY_SEPARATOR.length() - 1;
            } else {
                current.append(character);
            }
        }
        formattedEntries.add(current.toString());
        return formattedEntries;
    }

    public static Map<String, String> parse(String key) {
        Map<String, String> values = new LinkedHashMap<>();
        for (String formattedEntry : split(key)) {
            int separatorPosition = formattedEntry.indexOf(NAME_VALUE_SEPARATOR);
            if (separatorPosition < 0) {
                throw new IllegalArgumentException(
                        "Key entry [" + formattedEntry + "] has no value; expected name" + NAME_VALUE_SEPARATOR + "value"
                );
            }
            values.put(
                    formattedEntry.substring(0, separatorPosition),
                    unescape(formattedEntry.substring(separatorPosition + 1))
            );
        }
        return values;
    }

    private static String escape(String value) {
        String escaped = value
                .replace(String.valueOf(ESCAPE), "" + ESCAPE + ESCAPE)
                .replace(ENTRY_SEPARATOR, "" + ESCAPE + COLON + ESCAPE + COLON)
                .replace(String.valueOf(NAME_VALUE_SEPARATOR), "" + ESCAPE + NAME_VALUE_SEPARATOR);
        return endsWithUnpairedColon(value)
                ? escaped.substring(0, escaped.length() - 1) + ESCAPE + COLON
                : escaped;
    }

    private static boolean endsWithUnpairedColon(String value) {
        int trailingColons = 0;
        while (trailingColons < value.length() && value.charAt(value.length() - 1 - trailingColons) == COLON) {
            trailingColons++;
        }
        return trailingColons % 2 == 1;
    }

    private static String unescape(String value) {
        StringBuilder unescaped = new StringBuilder();
        for (int position = 0; position < value.length(); position++) {
            char character = value.charAt(position);
            if (character == ESCAPE && position + 1 < value.length()) {
                unescaped.append(value.charAt(++position));
            } else {
                unescaped.append(character);
            }
        }
        return unescaped.toString();
    }
}
