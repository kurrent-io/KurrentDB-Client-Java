package io.kurrent.dbclient.serialization;

import java.util.function.Consumer;

/**
 * Provides operation-specific serialization settings that override the global client configuration
 * for individual operations like reading from or appending to streams. This allows fine-tuning
 * serialization behavior on a per-operation basis without changing the client-wide settings.
 */
public class OperationSerializationSettings {
    /**
     * Controls whether messages should be automatically deserialized for this specific operation.
     * When enabled (the default), messages will be converted to their appropriate Java types.
     * When disabled, messages will be returned in their raw serialized form.
     */
    private AutomaticDeserialization automaticDeserialization = AutomaticDeserialization.ENABLED;

    /**
     * A callback that allows customizing serialization settings for this specific operation.
     * This can be used to override type mappings, serializers, or other settings just for
     * the scope of a single operation without affecting other operations.
     */
    private Consumer<KurrentDBClientSerializationSettings> configureSettings;

    /**
     * A pre-configured settings instance that disables automatic deserialization.
     * Use this when you need to access raw message data in its serialized form.
     */
    public static final OperationSerializationSettings DISABLED = new OperationSerializationSettings();

    static {
        DISABLED.automaticDeserialization = AutomaticDeserialization.DISABLED;
    }

    /**
     * Creates operation-specific serialization settings with custom configuration while keeping
     * automatic deserialization enabled. This allows operation-specific type mappings or
     * serializer settings without changing the global client configuration.
     *
     * @param configure A callback to customize serialization settings for this operation.
     * @return A configured instance of {@link OperationSerializationSettings} with enabled deserialization.
     */
    public static OperationSerializationSettings configure(Consumer<KurrentDBClientSerializationSettings> configure) {
        OperationSerializationSettings settings = new OperationSerializationSettings();
        settings.automaticDeserialization = AutomaticDeserialization.ENABLED;
        settings.configureSettings = configure;
        return settings;
    }

    // Getters
    public AutomaticDeserialization automaticDeserialization() {
        return automaticDeserialization;
    }

    public Consumer<KurrentDBClientSerializationSettings> configureSettings() {
        return configureSettings;
    }
}
