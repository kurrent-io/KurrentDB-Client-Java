package io.kurrent.dbclient.serialization;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;

public class JacksonSerializer implements Serializer {
    public static class Settings {
        public static final JsonMapper.Builder defaultBuilder = JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .propertyNamingStrategy(PropertyNamingStrategies.LOWER_CAMEL_CASE)
                .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
                .serializationInclusion(JsonInclude.Include.NON_NULL)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        
        private JsonMapper.Builder jsonMapperBuilder = defaultBuilder;

        public JsonMapper.Builder jsonMapperBuilder() {
            return jsonMapperBuilder;
        }

        public void jsonMapperBuilder(JsonMapper.Builder jsonMapperBuilder) {
            this.jsonMapperBuilder = jsonMapperBuilder;
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(JacksonSerializer.class);

    private final JsonMapper jsonMapper;

    public JacksonSerializer() {
        this(new Settings());
    }

    public JacksonSerializer(Settings settings) {
        jsonMapper = settings.jsonMapperBuilder().build();
    }

    @Override
    public byte[] serialize(Object value) {
        try {
            return jsonMapper.writeValueAsBytes(value);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <MessageType> Optional<MessageType> deserialize(Class<MessageType> eventClass, byte[] data) {
        try {
            MessageType result = jsonMapper.readValue(data, eventClass);

            return Optional.ofNullable(result);
        } catch (IOException e) {
            logger.warn("Error deserializing event {}", eventClass.getName(), e);
            return Optional.empty();
        }
    }
}
