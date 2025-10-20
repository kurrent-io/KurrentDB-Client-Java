package io.kurrent.dbclient;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.google.protobuf.ByteString;
import com.google.protobuf.Timestamp;
import com.google.protobuf.Duration;
import com.google.protobuf.Value;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
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
     */
    public static Map<String, Value> mapJsonToValueMap(byte[] jsonMetadata) {
        if (jsonMetadata == null || jsonMetadata.length == 0)
            return Collections.emptyMap();

        try {
            Map<String, Object> metadata = objectMapper.readValue(jsonMetadata, new TypeReference<Map<String, Object>>() {
            });
            return mapToValueMap(metadata);
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }

    /**
     * Converts a Map of metadata to a Map of DynamicValue objects.
     *
     * @param metadata the source metadata map
     * @return a map with DynamicValue objects
     */
    public static Map<String, Value> mapToValueMap(Map<String, Object> metadata) {
        if (metadata == null) {
            return Collections.emptyMap();
        }

        return metadata.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> mapToValue(entry.getValue())
                ));
    }

    /**
     * Converts a Java object to a DynamicValue protobuf message.
     *
     * @param source the source object
     * @return the corresponding DynamicValue
     */
    public static Value mapToValue(Object source) {
        if (source == null) {
            return Value.newBuilder()
                    .setNullValue(com.google.protobuf.NullValue.NULL_VALUE)
                    .build();
        }

        Value.Builder builder = Value.newBuilder();

        if (source instanceof String) {
            return builder.setStringValue((String) source).build();
        } else if (source instanceof Boolean) {
            return builder.setBoolValue((Boolean) source).build();
        } else if (source instanceof Integer) {
            return builder.setNumberValue((Integer) source).build();
        } else if (source instanceof Long) {
            return builder.setNumberValue((Long) source).build();
        } else if (source instanceof Float) {
            return builder.setNumberValue((Float) source).build();
        } else if (source instanceof Double) {
            return builder.setNumberValue((Double) source).build();
        } else if (source instanceof Instant) {
            Instant instant = (Instant) source;
            return builder.setStringValue(
                    Timestamp.newBuilder()
                            .setSeconds(instant.getEpochSecond())
                            .setNanos(instant.getNano())
                            .build().toString()
            ).build();
        } else if (source instanceof LocalDateTime) {
            LocalDateTime localDateTime = (LocalDateTime) source;
            Instant instant = localDateTime.atZone(java.time.ZoneOffset.UTC).toInstant();
            return builder.setStringValue(
                    Timestamp.newBuilder()
                            .setSeconds(instant.getEpochSecond())
                            .setNanos(instant.getNano())
                            .build().toString()
            ).build();
        } else if (source instanceof ZonedDateTime) {
            ZonedDateTime zonedDateTime = (ZonedDateTime) source;
            Instant instant = zonedDateTime.toInstant();
            return builder.setStringValue(
                    Timestamp.newBuilder()
                            .setSeconds(instant.getEpochSecond())
                            .setNanos(instant.getNano())
                            .build().toString()
            ).build();
        } else if (source instanceof java.time.Duration) {
            java.time.Duration duration = (java.time.Duration) source;
            return builder.setStringValue(
                    Duration.newBuilder()
                            .setSeconds(duration.getSeconds())
                            .setNanos(duration.getNano())
                            .build().toString()
            ).build();
        } else if (source instanceof byte[]) {
            return builder.setStringValue(ByteString.copyFrom((byte[]) source).toStringUtf8()).build();
        } else {
            // For any other type, convert to string
            return builder.setStringValue(source.toString()).build();
        }
    }
}
