package software.spool.core.adapter.jackson;

import com.fasterxml.jackson.databind.JsonNode;
import software.spool.core.port.serde.PayloadMapper;

public class PayloadMapperFactory {
    public static <I> PayloadMapper<I, JsonNode> jsonNode() {
        return p -> JacksonMapperFactory.json().valueToTree(p);
    }
}