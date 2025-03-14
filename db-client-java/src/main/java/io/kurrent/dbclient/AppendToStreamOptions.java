package io.kurrent.dbclient;

import io.kurrent.dbclient.serialization.OperationSerializationSettings;

import java.util.Optional;

/**
 * Options of the append stream request.
 */
public class AppendToStreamOptions extends OptionsWithStreamStateBase<AppendToStreamOptions> {
   private OperationSerializationSettings serializationSettings = null;

    private AppendToStreamOptions() {
    }

    /**
     * Returns optional serialization settings
     */
    public Optional<OperationSerializationSettings> serializationSettings() {
        return Optional.ofNullable(this.serializationSettings);
    }

    /**
     * Allows to customize or disable the automatic deserialization
     *
     * @param serializationSettings - expected revision.
     * @return updated options.
     */
    public AppendToStreamOptions serializationSettings(OperationSerializationSettings serializationSettings) {
        this.serializationSettings = serializationSettings;
        return this;
    }

    /**
     * Returns options with default values.
     */
    public static AppendToStreamOptions get() {
        return new AppendToStreamOptions();
    }
}
