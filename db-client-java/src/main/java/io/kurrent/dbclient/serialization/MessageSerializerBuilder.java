package io.kurrent.dbclient.serialization;

public final class MessageSerializerBuilder {
    public static MessageSerializer get(KurrentDBClientSerializationSettings settings) {
        return MessageSerializerImpl.from(settings);
    }
}
