package software.spool.core.adapter.jackson;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import software.spool.core.port.serde.RecordEnricher;

public class RecordEnricherFactory {
    private RecordEnricherFactory() {}

    public static <R> RecordEnricher<R, R> noOp() {
        return (records, fields) -> records;
    }

    public static RecordEnricher<JsonNode, JsonNode> json() {
        return (records, fields) -> records.map(record -> {
            if (!(record instanceof ObjectNode)) return record;
            ObjectNode copy = record.deepCopy();
            fields.forEach(f -> copy.set(f.target(), f.value()));
            return copy;
        });
    }
}
