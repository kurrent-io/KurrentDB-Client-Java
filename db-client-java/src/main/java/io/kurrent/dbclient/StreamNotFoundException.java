package io.kurrent.dbclient;

/**
 * When a stream is not found.
 */
public class StreamNotFoundException extends RuntimeException {
    private final String streamName;

    StreamNotFoundException(String streamName){
        this.streamName = streamName;
    }

    public String getStreamName() {
        return this.streamName;
    }
}
