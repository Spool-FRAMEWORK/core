package software.spool.core.port.serde;

import java.util.List;
import java.util.stream.Stream;

public interface RecordEnricher<R, E> {
    Stream<R> enrich(Stream<R> records, List<ExtractedField<E>> fields);
}
