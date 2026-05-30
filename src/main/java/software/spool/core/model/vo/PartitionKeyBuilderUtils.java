package software.spool.core.model.vo;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

class PartitionKeyBuilderUtils {
    private PartitionKeyBuilderUtils() {}

    static String datePrefix() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        return "year=" + now.getYear()
                + "::month=" + String.format("%02d", now.getMonthValue())
                + "::day=" + String.format("%02d", now.getDayOfMonth())
                + "::hour=" + String.format("%02d", now.getHour());
    }

    static String escapeValue(Object value) {
        return String.valueOf(value)
                .replace("\\", "\\\\")
                .replace("::", "\\:\\:")
                .replace("=", "\\=");
    }
}