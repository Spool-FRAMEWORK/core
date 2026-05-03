package software.spool.core.adapter.jackson;

import com.fasterxml.jackson.databind.JsonNode;
import software.spool.core.port.serde.EnrichmentRule;
import software.spool.core.port.serde.ExtractedField;
import software.spool.core.port.serde.PayloadExtractor;

import java.util.List;

public class PayloadExtractorFactory {

    private PayloadExtractorFactory() {}

    public static PayloadExtractor<JsonNode, JsonNode> withRules(List<EnrichmentRule> rules) {
        return payload -> rules.stream()
                .map(r -> new ExtractedField<>(r.target(), resolvePath(payload, r.source())))
                .filter(f -> !f.value().isMissingNode() && !f.value().isNull())
                .toList();
    }

    public static <P, E> PayloadExtractor<P, E> noOp() {
        return payload -> List.of();
    }

    private static JsonNode resolvePath(JsonNode root, String path) {
        JsonNode current = root;
        for (String segment : path.split("\\.")) {
            current = segment.matches("\\d+")
                    ? current.path(Integer.parseInt(segment))
                    : current.path(segment);
            if (current.isMissingNode()) return current;
        }
        return current;
    }
}
