package io.kurrent.dbclient;

/**
 * When a read or write operation was performed on a deleted stream.
 */
final public class StreamTombstonedException extends RuntimeException {
    private final String streamName;

    StreamTombstonedException(String streamName) {
        super(String.format("Stream '%s' is deleted", streamName));

        this.streamName = streamName;
    }

    public String getStreamName() {
        return streamName;
    }
}
