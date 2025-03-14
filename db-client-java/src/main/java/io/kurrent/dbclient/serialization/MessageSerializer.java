package io.kurrent.dbclient.serialization;

public interface MessageSerializer {
    MessageSerializer with(OperationSerializationSettings serializationSettings);

    void serialize();
}

