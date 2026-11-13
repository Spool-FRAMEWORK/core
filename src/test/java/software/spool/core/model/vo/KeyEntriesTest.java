package software.spool.core.model.vo;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class KeyEntriesTest {

    @Test
    void format_entries_joinsNameValuePairsInOrder() {
        String key = new KeyEntries().with("source", "boe").with("year", 2026).format();

        assertThat(key).isEqualTo("source=boe::year=2026");
    }

    @Test
    void parse_valuesWithSeparatorsInside_returnsTheOriginalValues() {
        String key = new KeyEntries()
                .with("title", "a::b=c")
                .with("path", "C:\\data\\")
                .with("time", "10:")
                .with("id", "42")
                .format();

        assertThat(KeyEntries.parse(key)).containsExactly(
                Map.entry("title", "a::b=c"),
                Map.entry("path", "C:\\data\\"),
                Map.entry("time", "10:"),
                Map.entry("id", "42")
        );
    }

    @Test
    void split_keyWithEscapedSeparator_keepsEachEntryWhole() {
        String key = new KeyEntries().with("title", "a::b").with("time", "10:").with("id", "42").format();

        assertThat(KeyEntries.split(key)).containsExactly("title=a\\:\\:b", "time=10\\:", "id=42");
    }

    @Test
    void join_entries_returnsTheKeyThatSplitSeparates() {
        String key = KeyEntries.join(List.of("year=2026", "month=11"));

        assertThat(KeyEntries.split(key)).containsExactly("year=2026", "month=11");
    }

    @Test
    void split_emptyKey_returnsNoEntries() {
        assertThat(KeyEntries.split("")).isEmpty();
    }

    @Test
    void format_valueEscapedLikeBefore_keepsExistingKeysUnchanged() {
        String key = new KeyEntries().with("title", "a::b=c\\d").format();

        assertThat(key).isEqualTo("title=a\\:\\:b\\=c\\\\d");
    }
}
