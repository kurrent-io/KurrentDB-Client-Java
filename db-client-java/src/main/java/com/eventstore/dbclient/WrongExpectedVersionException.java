package com.eventstore.dbclient;

import javax.validation.constraints.NotNull;

/**
 * When append request failed the optimistic concurrency on the server.
 */
public class WrongExpectedVersionException extends RuntimeException {
    private final String streamName;
    private final StreamState nextExpectedRevision;
    private final StreamState actualRevision;

    WrongExpectedVersionException(
            @NotNull String streamName,
            @NotNull StreamState nextExpected,
            @NotNull StreamState actualRevision) {
        super(String.format("Expected %s but got %s instead", nextExpected, actualRevision));
        this.streamName = streamName;
        this.nextExpectedRevision = nextExpected;
        this.actualRevision = actualRevision;
    }

    /**
     * Returns on which stream the error occurred.
     */
    public String getStreamName() {
        return streamName;
    }

    /**
     * Returns the expected revision by the request.
     */
    public StreamState getNextExpectedRevision() {
        return nextExpectedRevision;
    }

    /**
     * Returns the actual revision of the stream when the check was performed.
     */
    public StreamState getActualVersion() {
        return actualRevision;
    }
}
