package io.kurrent.dbclient.serialization;

/**
 * Controls whether the KurrentDB client should automatically deserialize message payloads
 * into their corresponding Java types based on the configured type mappings.
 */
public enum AutomaticDeserialization {
    /**
     * Disables automatic deserialization. Messages will be returned in their raw serialized form,
     * requiring manual deserialization by the application. Use this when you need direct access to the raw data
     * or when working with messages that don't have registered type mappings.
     */
    DISABLED(0),

    /**
     * Enables automatic deserialization. The client will attempt to convert messages into their appropriate
     * Java types using the configured serializers and type mappings. This simplifies working with strongly-typed
     * domain messages but requires proper type registration.
     */
    ENABLED(1);

    private final int value;

    AutomaticDeserialization(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
