package io.kurrent.dbclient.serialization;

public final class MessageSerializerBuilder {
    public static MessageSerializer get() {
        return new MessageSerializerImpl();
    }
}
