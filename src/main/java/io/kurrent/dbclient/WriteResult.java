package io.kurrent.dbclient;

import java.util.Objects;

/**
 * Returned after writing to a stream.
 */
public class WriteResult {
    private final StreamState nextExpectedRevision;
    private final Position logPosition;

    WriteResult(StreamState nextExpectedRevision, Position logPosition) {
        this.nextExpectedRevision = nextExpectedRevision;
        this.logPosition = logPosition;
    }

    /**
     * Next expected version of the stream.
     */
    public StreamState getNextExpectedRevision() {
        return nextExpectedRevision;
    }

    /**
     * Transaction log position of the write.
     */
    public Position getLogPosition() {
        return logPosition;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WriteResult that = (WriteResult) o;
        return nextExpectedRevision == that.nextExpectedRevision &&
                logPosition.equals(that.logPosition);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nextExpectedRevision, logPosition);
    }

    @Override
    public String toString() {
        return "WriteResult{" +
                "nextExpectedRevision=" + nextExpectedRevision +
                ", logPosition=" + logPosition +
                '}';
    }
}
