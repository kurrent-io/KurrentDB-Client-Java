package io.kurrent.dbclient.serialization;

import com.fasterxml.jackson.databind.json.JsonMapper;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Provides configuration options for messages serialization and deserialization in the KurrentDB client.
 */
public class KurrentDBClientSerializationSettings implements Cloneable {
    /**
     * The serializer responsible for handling JSON-formatted data. This serializer is used both for
     * serializing outgoing JSON messages and deserializing incoming JSON messages. If not specified,
     * a default System.Text.Json serializer will be used with standard settings.
     * <p>
     * That also allows you to bring your custom JSON serializer implementation (e.g. JSON.NET)
     */
    private Serializer jsonSerializer;


    /**
     * The serializer responsible for handling binary data formats. This is used when working with
     * binary-encoded messages rather than text-based formats (e.g. Protobuf or Avro). Required when storing
     * or retrieving content with "application/octet-stream" content type
     */
    private Serializer bytesSerializer;

    /**
     * Determines which serialization format (JSON or binary) is used by default when writing messages
     * where the content type isn't explicitly specified. The default content type is "application/json"
     */
    private ContentType defaultContentType = ContentType.JSON;

    /**
     * Defines the custom strategy used to map between the type name stored in messages and Java type names.
     * If not provided the default {@link io.kurrent.dbclient.serialization.DefaultMessageTypeNamingStrategy} will be used.
     * It resolves the class name to the format: "{stream category name}-{Class Message Type}".
     * You can provide your own implementation of {@link io.kurrent.dbclient.serialization.MessageTypeNamingStrategy}
     * and register it here to override the default behavior
     */
    private MessageTypeNamingStrategy messageTypeNamingStrategy;

    /**
     * Allows to register mapping of Java message types to their corresponding message type names used in serialized messages.
     */
    private Map<Class<?>, String> messageTypeMap = new HashMap<>();

    /**
     * Registers Java message types that can be appended to the specific stream category.
     * Types will have message type names resolved based on the used {@link io.kurrent.dbclient.serialization.MessageTypeNamingStrategy}
     */
    private Map<String, Class<?>[]> categoryMessageTypesMap = new HashMap<>();

    /**
     * Specifies the Java type that should be used when deserializing metadata for all events.
     * When set, the client will attempt to deserialize event metadata into this type.
     * If not provided, {@link io.kurrent.dbclient.serialization.TracingMetadata} will be used.
     */
    private Class<?> defaultMetadataType;
    
    /**
     * Creates a new instance of serialization settings with either default values or custom configuration.
     * This factory method is the recommended way to create serialization settings for the KurrentDB client.
     *
     * @param configure Optional callback to customize the settings. If null, default settings are used.
     * @return A fully configured instance ready to be used with the KurrentDB client.
     * <pre>
     * {@code
     * KurrentDBClientSerializationSettings settings = KurrentDBClientSerializationSettings.get(options -> {
     *     options.registerMessageType(UserRegistered.class, "user-registered");
     *     options.registerMessageType(UserRoleAssigned.class, "user-role-assigned");
     *     options.registerMessageTypeForCategory(UserRegistered.class, "user-registered");
     * });
     * }
     * </pre>
     */
    public static KurrentDBClientSerializationSettings get(
            Consumer<KurrentDBClientSerializationSettings> configure
    ) {
        KurrentDBClientSerializationSettings settings = get();

        configure.accept(settings);

        return settings;
    }
    
    /**
     * Creates a new instance of serialization settings with either default values.
     * This factory method is the recommended way to create serialization settings for the KurrentDB client.
     *
     * @return A fully configured default instance ready to be used with the KurrentDB client.
     */
    public static KurrentDBClientSerializationSettings get() {
        return new KurrentDBClientSerializationSettings();
    }

    /**
     * Configures the JSON serializer using custom options while inheriting from the default System.Text.Json settings.
     * This allows fine-tuning serialization behavior such as case sensitivity, property naming, etc.
     *
     * @param configure A function that receives the default options and returns modified options.
     * @return The current instance for method chaining.
     * @example
     * <pre>
     * {@code
     * settings.useJsonSettings(builder -> 
     *     builder.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true)
     *             .propertyNamingStrategy(PropertyNamingStrategies.KEBAB_CASE)
     * );
     * }
     * </pre>
     */
    public KurrentDBClientSerializationSettings useJsonSettings(
            Function<JsonMapper.Builder, JsonMapper.Builder> configure
    ) {
        JacksonSerializer.Settings settings = new JacksonSerializer.Settings();
        settings.jsonMapperBuilder(configure.apply(settings.jsonMapperBuilder()));
        
        jsonSerializer = new JacksonSerializer(settings);

        return this;
    }

    /**
     * Sets a custom JSON serializer implementation.
     * That also allows you to bring your custom JSON serializer implementation (e.g. Jackson)
     *
     * @param serializer The serializer to use for JSON content.
     * @return The current instance for method chaining.
     */
    public KurrentDBClientSerializationSettings useJsonSerializer(Serializer serializer) {
        jsonSerializer = serializer;

        return this;
    }

    /**
     * Sets a custom binary serializer implementation.
     * That also allows you to bring your custom binary serializer implementation (e.g. Protobuf or Avro)
     *
     * @param serializer The serializer to use for binary content.
     * @return The current instance for method chaining.
     */
    public KurrentDBClientSerializationSettings useBytesSerializer(Serializer serializer) {
        bytesSerializer = serializer;

        return this;
    }

    /**
     * Configures a custom message type naming strategy.
     *
     * @param <CustomMessageTypeResolutionStrategy> The type of naming strategy to use.
     * @param strategyClass                          The class of the naming strategy to instantiate.
     * @return The current instance for method chaining.
     */
    public <CustomMessageTypeResolutionStrategy extends MessageTypeNamingStrategy> KurrentDBClientSerializationSettings useMessageTypeNamingStrategy(
            Class<CustomMessageTypeResolutionStrategy> strategyClass
    ) {
        try {
            return useMessageTypeNamingStrategy(strategyClass.getDeclaredConstructor().newInstance());
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new RuntimeException("Failed to instantiate message type naming strategy", e);
        }
    }

    /**
     * Configures a custom message type naming strategy.
     *
     * @param messageTypeNamingStrategy The naming strategy instance to use.
     * @return The current instance for method chaining.
     */
    public KurrentDBClientSerializationSettings useMessageTypeNamingStrategy(
            MessageTypeNamingStrategy messageTypeNamingStrategy
    ) {
        this.messageTypeNamingStrategy = messageTypeNamingStrategy;

        return this;
    }

    /**
     * Associates a message type with a specific stream category to enable automatic deserialization.
     * In event sourcing, streams are often prefixed with a category (e.g., "user-123", "order-456").
     * This method tells the client which message types can appear in streams of a given category.
     *
     * @param <T>          The event or message type that can appear in the category's streams.
     * @param categoryName The category prefix (e.g., "user", "order", "account").
     * @param clazz        The class representing the event type.
     * @return The current instance for method chaining.
     * @example
     * <pre>
     * {@code
     * // Register event types that can appear in user streams
     * settings.registerMessageTypeForCategory(UserCreated.class, "user")
     *        .registerMessageTypeForCategory(UserUpdated.class, "user")
     *        .registerMessageTypeForCategory(UserDeleted.class, "user");
     * }
     * </pre>
     */
    public <T> KurrentDBClientSerializationSettings registerMessageTypeForCategory(Class<T> clazz, String categoryName) {
        return registerMessageTypeForCategory(categoryName, clazz);
    }

    /**
     * Registers multiple message types for a specific stream category.
     *
     * @param categoryName The category name to register the types with.
     * @param types        The message types to register.
     * @return The current instance for method chaining.
     */
    public KurrentDBClientSerializationSettings registerMessageTypeForCategory(String categoryName, Class<?>... types) {
        if (categoryMessageTypesMap.containsKey(categoryName)) {
            Class<?>[] current = categoryMessageTypesMap.get(categoryName);
            Class<?>[] combined = Arrays.copyOf(current, current.length + types.length);
            System.arraycopy(types, 0, combined, current.length, types.length);
            categoryMessageTypesMap.put(categoryName, combined);
        } else {
            categoryMessageTypesMap.put(categoryName, types);
        }

        return this;
    }

    /**
     * Maps a Java type to a specific message type name that will be stored in the message metadata.
     * This mapping is used during automatic deserialization, as it tells the client which Java class
     * to instantiate when encountering a message with a particular type name in the database.
     *
     * @param <T>      The Java type to register (typically a message class).
     * @param clazz    The class representing the message type.
     * @param typeName The string identifier to use for this type in the database.
     * @return The current instance for method chaining.
     * @remarks The type name is often different from the Java type name to support versioning and evolution
     * of your domain model without breaking existing stored messages.
     * @example
     * <pre>
     * {@code
     * // Register me types with their corresponding type identifiers
     * settings.registerMessageType(UserCreated.class, "user-created-v1")
     *        .registerMessageType(OrderPlaced.class, "order-placed-v2");
     * }
     * </pre>
     */
    public <T> KurrentDBClientSerializationSettings registerMessageType(Class<T> clazz, String typeName) {
        messageTypeMap.put(clazz, typeName);

        return this;
    }

    /**
     * Registers multiple message types with their corresponding type names.
     *
     * @param typeMap Map mapping types to their type names.
     * @return The current instance for method chaining.
     */
    public KurrentDBClientSerializationSettings registerMessageTypes(Map<Class<?>, String> typeMap) {
        messageTypeMap.putAll(typeMap);

        return this;
    }

    /**
     * Configures a strongly-typed metadata class for all messages in the system.
     * This enables accessing metadata properties in a type-safe manner rather than using dynamic objects.
     *
     * @param <T>   The metadata class type containing properties matching the expected metadata fields.
     * @param clazz The class representing the metadata type.
     * @return The current instance for method chaining.
     */
    public <T> KurrentDBClientSerializationSettings useMetadataType(Class<T> clazz) {
        defaultMetadataType = clazz;

        return this;
    }

    /**
     * Configures which serialization format (JSON or binary) is used by default when writing messages
     * where the content type isn't explicitly specified. The default content type is "application/json"
     *
     * @param contentType The serialization format content type
     * @return The current instance for method chaining.
     */
    public KurrentDBClientSerializationSettings useContentType(ContentType contentType) {
        defaultContentType = contentType;

        return this;
    }

    @Override
    public KurrentDBClientSerializationSettings clone() {
        try {
            KurrentDBClientSerializationSettings clone = (KurrentDBClientSerializationSettings) super.clone();
            clone.bytesSerializer = this.bytesSerializer;
            clone.jsonSerializer = this.jsonSerializer;
            clone.defaultContentType = this.defaultContentType;
            clone.messageTypeMap = new HashMap<>(this.messageTypeMap);
            clone.categoryMessageTypesMap = new HashMap<>(this.categoryMessageTypesMap);
            clone.messageTypeNamingStrategy = this.messageTypeNamingStrategy;
            clone.defaultMetadataType = this.defaultMetadataType;
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
    }

    // Getters
    public Optional<Serializer> jsonSerializer() {
        return Optional.ofNullable(jsonSerializer);
    }

    public Optional<Serializer> bytesSerializer() {
        return Optional.ofNullable(bytesSerializer);
    }

    public ContentType defaultContentType() {
        return defaultContentType;
    }

    public Optional<MessageTypeNamingStrategy> messageTypeNamingStrategy() {
        return Optional.ofNullable(messageTypeNamingStrategy);
    }

    public Map<Class<?>, String> messageTypeMap() {
        return messageTypeMap;
    }

    public Map<String, Class<?>[]> categoryMessageTypesMap() {
        return categoryMessageTypesMap;
    }

    public Class<?> defaultMetadataType() {
        return defaultMetadataType;
    }
}
