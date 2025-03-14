package io.kurrent.dbclient.serialization;

import java.util.Arrays;

public class MessageTypeNamingResolutionContext {
    private final String categoryName;

    public MessageTypeNamingResolutionContext(String streamName) {
        this.categoryName = streamName;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public static MessageTypeNamingResolutionContext fromStreamName(String streamName) {
        return new MessageTypeNamingResolutionContext(
                Arrays.stream(streamName.split("-")).findFirst().orElse("no_stream_category")
        );
    }
}
