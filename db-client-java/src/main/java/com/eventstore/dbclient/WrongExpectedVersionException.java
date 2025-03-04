package com.eventstore.dbclient;

import javax.validation.constraints.NotNull;

/**
 * When append request failed the optimistic concurrency on the server.
 */
public class WrongExpectedVersionException extends RuntimeException {
    private final String streamName;
    private final StreamState expectedState;
    private final StreamState actualState;

    WrongExpectedVersionException(
            @NotNull String streamName,
            @NotNull StreamState expectedState,
            @NotNull StreamState actualState) {
        super(String.format("Expected %s but got %s instead", expectedState, actualState));
        this.streamName = streamName;
        this.expectedState = expectedState;
        this.actualState = actualState;
    }

    /**
     * Returns on which stream the error occurred.
     */
    public String getStreamName() {
        return streamName;
    }

    /**
     * Returns the expected stream state by the request.
     */
    public StreamState getExpectedState() {
        return expectedState;
    }

    /**
     * Returns the actual stream state when the check was performed.
     */
    public StreamState getActualState() {
        return actualState;
    }
}
