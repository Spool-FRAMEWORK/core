package software.spool.core.model.vo;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

class PartitionKeyBuilderUtils {
    private PartitionKeyBuilderUtils() {}

    static KeyEntries dateEntries() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        return new KeyEntries()
                .with("year", now.getYear())
                .with("month", String.format("%02d", now.getMonthValue()))
                .with("day", String.format("%02d", now.getDayOfMonth()))
                .with("hour", String.format("%02d", now.getHour()));
    }
}
