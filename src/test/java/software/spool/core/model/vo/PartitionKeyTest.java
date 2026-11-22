package software.spool.core.model.vo;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PartitionKeyTest {
    private static final PartitionKey PAYMENTS_DAY_10 = PartitionKey.ofEntriesWithoutDate()
            .with("source", "payments").with("day", "10").build();

    @Test
    void contains_entryWhoseValueOnlyStartsTheSame_isFalse() {
        PartitionKey day1 = PartitionKey.ofEntriesWithoutDate().with("day", "1").build();

        assertThat(PAYMENTS_DAY_10.contains(day1)).isFalse();
    }

    @Test
    void contains_sourceWhoseNameOnlyStartsTheSame_isFalse() {
        PartitionKey pay = PartitionKey.ofEntriesWithoutDate().with("source", "pay").build();

        assertThat(PAYMENTS_DAY_10.contains(pay)).isFalse();
    }

    @Test
    void contains_wholeEntriesInAnyOrder_isTrue() {
        PartitionKey scope = PartitionKey.ofEntriesWithoutDate().with("day", "10").with("source", "payments").build();

        assertThat(PAYMENTS_DAY_10.contains(scope)).isTrue();
    }

    @Test
    void contains_emptyScope_isTrue() {
        assertThat(PAYMENTS_DAY_10.contains(new PartitionKey(""))).isTrue();
    }
}
