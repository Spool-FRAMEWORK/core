package software.spool.core.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.spool.core.exception.DeserializationException;
import software.spool.core.port.MessageDeserializer;

public class JacksonMessageDeserializer<R> implements MessageDeserializer<R> {
    private final ObjectMapper mapper;

    public JacksonMessageDeserializer(ObjectMapper mapper) { this.mapper = mapper; }

    @Override
    public <T> T deserialize(R payload, Class<T> type) throws DeserializationException {
        try {
            return mapper.readValue(payload.toString(), type);
        } catch (JsonProcessingException e) {
            throw new DeserializationException(payload.toString(), e);
        }
    }
}
