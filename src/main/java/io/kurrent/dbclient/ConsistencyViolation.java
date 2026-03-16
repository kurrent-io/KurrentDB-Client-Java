package io.kurrent.dbclient;

/**
 * Reports a single consistency check that was violated during an AppendRecords operation.
 */
public class ConsistencyViolation {
    private final int checkIndex;
    private final String stream;
    private final StreamState expectedState;
    private final StreamState actualState;

    /**
     * Creates a new ConsistencyViolation.
     *
     * @param checkIndex index of the check in the original consistency checks list.
     * @param stream the name of the stream whose state was checked.
     * @param expectedState the expected state of the stream.
     * @param actualState the actual state of the stream at the time the check was evaluated.
     */
    public ConsistencyViolation(int checkIndex, String stream, StreamState expectedState, StreamState actualState) {
        this.checkIndex = checkIndex;
        this.stream = stream;
        this.expectedState = expectedState;
        this.actualState = actualState;
    }

    /**
     * Returns the index of the check in the original consistency checks list.
     */
    public int getCheckIndex() {
        return checkIndex;
    }

    /**
     * Returns the stream name whose state was checked.
     */
    public String getStream() {
        return stream;
    }

    /**
     * Returns the expected state of the stream.
     */
    public StreamState getExpectedState() {
        return expectedState;
    }

    /**
     * Returns the actual state of the stream at the time the check was evaluated.
     */
    public StreamState getActualState() {
        return actualState;
    }
}
