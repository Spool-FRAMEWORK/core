package software.spool.core.model.quarantine;

import software.spool.core.model.vo.IdempotencyKey;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * One page of quarantine records, in the order of their keys, and the key to ask the next page after, when there is
 * one.
 */
public record QuarantinePage(List<QuarantinedRecord> records, Optional<IdempotencyKey> next) {
    public QuarantinePage {
        records = List.copyOf(records);
        Objects.requireNonNull(next, "next is required");
    }

    /**
     * The page a request asks for, out of all the records in its status.
     */
    public static QuarantinePage of(Collection<QuarantinedRecord> recordsInStatus, QuarantinePageRequest request) {
        List<QuarantinedRecord> after = recordsInStatus.stream()
                .filter(quarantinedRecord -> quarantinedRecord.status() == request.status())
                .filter(quarantinedRecord -> request.after()
                        .map(cursor -> quarantinedRecord.idempotencyKey().value().compareTo(cursor.value()) > 0)
                        .orElse(true))
                .sorted(Comparator.comparing(quarantinedRecord -> quarantinedRecord.idempotencyKey().value()))
                .limit(request.limit() + 1L)
                .toList();
        List<QuarantinedRecord> page = after.subList(0, Math.min(request.limit(), after.size()));
        Optional<IdempotencyKey> next = after.size() > request.limit()
                ? Optional.of(page.getLast().idempotencyKey())
                : Optional.empty();
        return new QuarantinePage(page, next);
    }
}
