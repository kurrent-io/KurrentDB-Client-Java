package io.kurrent.dbclient.serialization;

import java.util.function.Consumer;

/**
 * Provides configuration options for messages serialization and deserialization in the KurrentDB client.
 */
public class KurrentDBClientSerializationSettings {
    /**
     * Creates a new instance of serialization settings with either default values or custom configuration.
     * This factory method is the recommended way to create serialization settings for the KurrentDB client.
     * @param configure Optional callback to customize the settings. If null, default settings are used.
     * @return A fully configured instance ready to be used with the KurrentDB client.
     * <pre>{@code
     * var settings = KurrentDBClientSerializationSettings.get(options -> {
     *     options.<UserRegistered>RegisterMessageType("user-created");
     *     options.<UserRoleAssigned>RegisterMessageType("user-role-assigned");
     * });
     * }</pre>
     */
    public static KurrentDBClientSerializationSettings get(
            Consumer<KurrentDBClientSerializationSettings> configure
    ) {
        KurrentDBClientSerializationSettings settings = get();

        configure.accept(settings);

        return settings;
    }

    public static KurrentDBClientSerializationSettings get() {
        return new KurrentDBClientSerializationSettings();
    }
}
