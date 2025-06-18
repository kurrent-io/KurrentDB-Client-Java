package io.kurrent.dbclient.serialization;

public class MessageSerializationContext {
    private final MessageTypeNamingResolutionContext namingResolution;

    public MessageSerializationContext(MessageTypeNamingResolutionContext namingResolution) {
        this.namingResolution = namingResolution;
    }

    public MessageTypeNamingResolutionContext namingResolution() {
        return namingResolution;
    }
}
