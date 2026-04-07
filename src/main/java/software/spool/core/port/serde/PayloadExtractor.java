package software.spool.core.port.serde;

import java.util.List;

public interface PayloadExtractor<P, E> {
    List<ExtractedField<E>> extract(P payload);
}
