package software.spool.core.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.spool.core.exception.DeserializationException;
import software.spool.core.port.PayloadDeserializer;

public class JacksonPayloadDeserializer<T> implements PayloadDeserializer<String, T> {
    private final ObjectMapper mapper;
    private final Class<T> type;

    public JacksonPayloadDeserializer(ObjectMapper mapper, Class<T> type) {
        this.mapper = mapper;
        this.type = type;
    }

    @Override
    public T deserialize(String payload) throws DeserializationException {
        try {
            return mapper.readValue(payload, type);
        } catch (JsonProcessingException e) {
            throw new DeserializationException(payload, e);
        }
    }
}

