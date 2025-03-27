package io.kurrent.dbclient;

import java.util.List;

/**
 * Returned after a successful read operation.
 */
public class ReadResult {
    private final List<ResolvedEvent> events;
    private final long firstStreamPosition;
    private final long lastStreamPosition;
    private final Position lastAllStreamPosition;

    ReadResult(List<ResolvedEvent> events, long firstStreamPosition, long lastStreamPosition, Position lastAllStreamPosition) {
        this.events = events;
        this.firstStreamPosition = firstStreamPosition;
        this.lastStreamPosition = lastStreamPosition;
        this.lastAllStreamPosition = lastAllStreamPosition;
    }

    /**
     * Returns all the events of the read operation.
     */
    public List<ResolvedEvent> getEvents() {
        return this.events;
    }

    /**
     * When reading from a regular stream, returns the first event revision number of the stream.
     */
    public long getFirstStreamPosition() {
        return firstStreamPosition;
    }

    /**
     * When reading from a regular stream, returns the last event revision number of the stream.
     */
    public long getLastStreamPosition() {
        return lastStreamPosition;
    }

    /**
     * When reading from $all stream, returns the last event position.
     * @return null if reading from a regular stream.
     */
    public Position getLastAllStreamPosition() {
        return lastAllStreamPosition;
    }

    @Override
    public String toString() {
        return "ReadResult{" +
                "events=" + events +
                ", firstStreamPosition=" + firstStreamPosition +
                ", lastStreamPosition=" + lastStreamPosition +
                ", lastAllStreamPosition=" + lastAllStreamPosition +
                '}';
    }
}
