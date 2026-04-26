package software.spool.core.adapter.jackson;

import com.fasterxml.jackson.databind.JsonNode;
import software.spool.core.port.serde.PayloadLocator;

import java.util.Objects;

public class PayloadLocatorFactory {
    public static PayloadLocator<JsonNode> fromRootPath(String rootPath) {
        if (Objects.isNull(rootPath) || rootPath.isEmpty()) return noOp();
        return record -> record.at("/" + rootPath.replace(".", "/"));
    }

    public static <P> PayloadLocator<P> noOp() {
        return record -> record;
    }
}
