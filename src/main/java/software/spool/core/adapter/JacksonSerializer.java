package software.spool.core.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.spool.core.exception.SerializationException;
import software.spool.core.port.RecordSerializer;

public class JacksonSerializer<T> implements RecordSerializer<T> {
    private final ObjectMapper mapper;

    public JacksonSerializer(ObjectMapper mapper) { this.mapper = mapper; }

    @Override
    public String serialize(T record) throws SerializationException {
        try {
            return mapper.writeValueAsString(record);
        } catch (JsonProcessingException e) {
            throw new SerializationException(record.toString(), e);
        }
    }
}
