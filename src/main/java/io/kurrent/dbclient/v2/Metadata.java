package io.kurrent.dbclient.v2;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.nio.ByteBuffer;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Represents a collection of metadata as key-value pairs with additional helper methods.
 */
public class Metadata extends HashMap<String, Object> {
    /**
     * Initializes a new, empty instance of the Metadata class.
     */
    public Metadata() {
        super();
    }

    /**
     * Initializes a new instance of the Metadata class from an existing metadata instance.
     *
     * @param metadata The metadata to copy from.
     */
    public Metadata(Metadata metadata) {
        super(metadata);
    }

    /**
     * Initializes a new instance of the Metadata class from a dictionary.
     *
     * @param dictionary The dictionary to copy from.
     */
    public Metadata(Map<String, Object> dictionary) {
        super(dictionary);
    }

    /**
     * Adds or updates a key-value pair in the metadata and returns the metadata instance.
     *
     * @param key   The key to add or update.
     * @param value The value to set.
     * @param <T>   The type of the value.
     * @return The metadata instance (this) to enable method chaining.
     */
    public <T> Metadata set(String key, T value) {
        this.put(key, value);
        return this;
    }

    /**
     * Gets a typed value from the metadata.
     *
     * @param key      The key to retrieve.
     * @param type     The class of the type T.
     * @param <T>      The type to cast the value to.
     * @return The value cast to type T, or null if the key is not found or the value can't be cast to type T.
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> type) {
        Object value = get(key);
        if (containsKey(key) && type.isInstance(value))
            return (T) value;

        return null;
    }

    /**
     * Gets a typed value from the metadata, with automatic type conversion where appropriate.
     *
     * @param metadata     The metadata object.
     * @param key          The key to retrieve.
     * @param defaultValue The default value to return if the key is not found or the value can't be cast to type T.
     * @param type         The class of the type T.
     * @param <T>          The type to cast the value to.
     * @return The value cast to type T, or the default value if the key is not found or the value can't be cast to type T.
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, T defaultValue, Class<T> type) {
        Object value = get(key);
        if (containsKey(key) && type.isInstance(value)) {
            return (T) value;
        }
        return defaultValue;
    }

    /**
     * Tries to get a typed value from the metadata, with automatic type conversion where appropriate.
     *
     * @param key      The key to retrieve.
     * @param type     The class of the type T.
     * @param <T>      The type to get or convert to.
     */
    @SuppressWarnings("unchecked")
    public <T> Optional<T> tryGet(String key, Class<T> type) {
        Object obj = get(key);
        if (!containsKey(key)) {
            return Optional.empty();
        }

        // Direct type match
        if (type.isInstance(obj)) {
            return Optional.of((T) obj);
        }

        // Handle byte array conversions
        if (type == byte[].class || type == ByteBuffer.class) {
            if (obj instanceof byte[]) {
                if (type == byte[].class)
                    return Optional.of((T) obj);
                else
                    return Optional.of((T) ByteBuffer.wrap((byte[]) obj));
            }

            if (obj instanceof ByteBuffer) {
                if (type == byte[].class)
                    return Optional.of((T) ((ByteBuffer) obj).array());
                else
                    return Optional.of((T) obj);
            }

            return Optional.empty();
        }

        // Convert string representation for various types
        String stringValue = obj.toString();
        if (stringValue == null)
            return Optional.empty();

        // Handle common value types with parsing
        if (type == Boolean.class || type == boolean.class) {
            Boolean value = BooleanUtils.toBooleanObject(stringValue);

            if (value == null)
                return Optional.empty();

            return Optional.of((T) value);
        }

        if (type == Byte.class || type == byte.class) {
            byte value = NumberUtils.toByte(stringValue);
            if (value == 0 && !stringValue.equalsIgnoreCase("0"))
                return Optional.empty();

            return Optional.of((T) Byte.valueOf(value));
        }

        if (type == Short.class || type == short.class) {
            short value = NumberUtils.toShort(stringValue);
            if (value == 0 && !stringValue.equalsIgnoreCase("0"))
                return Optional.empty();

            return Optional.of((T) Short.valueOf(stringValue));
        }

        if (type == Integer.class || type == int.class) {
            int value = NumberUtils.toInt(stringValue);
            if (value == 0 && !stringValue.equalsIgnoreCase("0"))
                return Optional.empty();

            return Optional.of((T) Integer.valueOf(stringValue));
        }

        if (type == Long.class || type == long.class) {
            long value = NumberUtils.toLong(stringValue);
            if (value == 0 && !stringValue.equalsIgnoreCase("0"))
                return Optional.empty();

            return Optional.of((T) Long.valueOf(stringValue));
        }

        if (type == Float.class || type == float.class) {
            float value = NumberUtils.toFloat(stringValue, Float.NaN);
            if (Float.isNaN(value))
                return Optional.empty();

            return Optional.of((T) Float.valueOf(value));
        }

        if (type == Double.class || type == double.class) {
            double value = NumberUtils.toDouble(stringValue, Double.NaN);
            if (Double.isNaN(value))
                return Optional.empty();

            return Optional.of((T) Double.valueOf(value));

        }

        if (type == Character.class || type == char.class) {
            if (stringValue.length() == 1)
                return Optional.of((T) Character.valueOf(stringValue.charAt(0)));
        }

        try {
            if (type == UUID.class)
                return Optional.of((T) UUID.fromString(stringValue));

            if (type == Instant.class)
                return Optional.of((T) Instant.parse(stringValue));

            if (type == LocalDate.class)
                return Optional.of((T) LocalDate.parse(stringValue));

            if (type == LocalTime.class)
                return Optional.of((T) LocalTime.parse(stringValue));

        } catch (DateTimeParseException | IllegalArgumentException e) {
            return Optional.empty();
        }

        return Optional.empty();
    }
}
