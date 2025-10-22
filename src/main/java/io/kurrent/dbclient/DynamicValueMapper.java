package io.kurrent.dbclient;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.google.protobuf.Value;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Utility class for converting Java objects to DynamicValue protobuf messages.
 */
public class DynamicValueMapper {
    private static final JsonMapper objectMapper = new JsonMapper();

    /**
     * Converts JSON byte array metadata to a Map of DynamicValue objects.
     *
     * @param jsonMetadata the source metadata as JSON bytes
     * @return a map with DynamicValue objects
     * @throws IllegalArgumentException if any metadata value is not a string
     */
    public static Map<String, Value> mapJsonToValueMap(byte[] jsonMetadata) {
        if (jsonMetadata == null || jsonMetadata.length == 0)
            return Collections.emptyMap();

        try {
            Map<String, Object> metadata = objectMapper.readValue(jsonMetadata, new TypeReference<Map<String, Object>>() {
            });
            return mapToValueMap(metadata);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Converts a Map of metadata to a Map of DynamicValue objects.
     *
     * @param metadata the source metadata map
     * @return a map with DynamicValue objects
     * @throws IllegalArgumentException if any metadata value is not a string
     */
    public static Map<String, Value> mapToValueMap(Map<String, ?> metadata) {
        if (metadata == null) {
            return Collections.emptyMap();
        }

        for (Map.Entry<String, ?> entry : metadata.entrySet()) {
            if (entry.getValue() != null && !(entry.getValue() instanceof String)) {
                throw new IllegalArgumentException(
                        String.format("Metadata value for key '%s' must be a string, but was %s",
                                entry.getKey(),
                                entry.getValue().getClass().getSimpleName())
                );
            }
        }

        Value.Builder builder = Value.newBuilder();

        return metadata.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> builder.setStringValue((String) entry.getValue()).build()));
    }
}
