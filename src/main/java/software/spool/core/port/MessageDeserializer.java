package software.spool.core.port;

import software.spool.core.exception.DeserializationException;

public interface MessageDeserializer<R> {
    <T> T deserialize(R message, Class<T> type) throws DeserializationException;
}
