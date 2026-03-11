package software.spool.core.port.decorator;

import software.spool.core.exception.DeserializationException;
import software.spool.core.exception.SpoolException;

public class SafeMessageDeserializer<R> implements MessageDeserializer<R> {
    private final MessageDeserializer<R> deserializer;

    public SafeMessageDeserializer(MessageDeserializer<R> deserializer) {
        this.deserializer = deserializer;
    }

    @Override
    public <T> T deserialize(R message, Class<T> type) throws DeserializationException {
        try {
            return deserializer.deserialize(message, type);
        } catch (SpoolException e) { throw e; }
        catch (Exception e) { throw new DeserializationException(message.toString(), e.getMessage()); }
    }
}
