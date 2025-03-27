package io.kurrent.dbclient;

/**
 * When a read or write operation was performed on a deleted stream.
 */
final public class StreamDeletedException extends RuntimeException {
    private final String streamName;

    StreamDeletedException(String streamName) {
        super(String.format("Stream '%s' is deleted", streamName));

        this.streamName = streamName;
    }

    public String getStreamName() {
        return streamName;
    }
}
