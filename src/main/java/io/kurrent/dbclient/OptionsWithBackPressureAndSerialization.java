package io.kurrent.dbclient;

import java.util.Optional;

import io.kurrent.dbclient.serialization.OperationSerializationSettings;

class OptionsWithBackPressureAndSerialization<T> extends OptionsWithBackPressure<T> {
    public OperationSerializationSettings serializationSettings;

    protected OptionsWithBackPressureAndSerialization(OperationKind kind) {
        super(kind);
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
    @SuppressWarnings("unchecked")
    public T serializationSettings(OperationSerializationSettings serializationSettings) {
        this.serializationSettings = serializationSettings;
        return (T)this;
    }
}
