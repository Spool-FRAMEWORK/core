package software.spool.core.port.bus;

import java.util.Map;

public record BrokerMessage<E>(
    E payload,
    String key,
    Map<String, String> headers
) {
    public BrokerMessage {
        headers = headers == null ? Map.of() : Map.copyOf(headers);
    }
}