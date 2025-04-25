package io.kurrent.dbclient;

import io.kurrent.dbclient.serialization.OperationSerializationSettings;

import java.util.Optional;

/**
 * Options of the subscribe persistent subscription request.
 */
public class SubscribePersistentSubscriptionOptions extends OptionsBase<SubscribePersistentSubscriptionOptions> {
    public OperationSerializationSettings serializationSettings;
    private int bufferSize;

    private SubscribePersistentSubscriptionOptions() {
        super(OperationKind.Streaming);
        this.bufferSize = 10;
    }

    /**
     * Returns options with default values.
     */
    public static SubscribePersistentSubscriptionOptions get() {
        return new SubscribePersistentSubscriptionOptions();
    }

    int getBufferSize() {
        return bufferSize;
    }

    /**
     * Persistent subscription's buffer size.
     */
    public SubscribePersistentSubscriptionOptions bufferSize(int value) {
        bufferSize = value;
        return this;
    }


    /**
     * Allows to customize or disable the automatic deserialization.
     */
    public Optional<OperationSerializationSettings> serializationSettings() {
        return Optional.ofNullable(serializationSettings);
    }

    /**
     * Customize or disable the automatic deserialization.
     */
    public SubscribePersistentSubscriptionOptions serializationSettings(OperationSerializationSettings serializationSettings) {
        this.serializationSettings = serializationSettings;
        return this;
    }   
}
