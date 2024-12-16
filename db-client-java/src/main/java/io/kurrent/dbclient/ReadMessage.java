package io.kurrent.dbclient;

import io.kurrent.dbclient.proto.streams.StreamsOuterClass;

/**
 * Received when performing a regular read operation (not a subscription).
 */
public final class ReadMessage {
    private Long firstStreamPosition;
    private Long lastStreamPosition;
    private Position lastAllPosition;
    private ResolvedEvent event;

    ReadMessage(StreamsOuterClass.ReadResp resp) {
        if (resp.hasLastAllStreamPosition()) {
            lastAllPosition = new Position(resp.getLastAllStreamPosition().getCommitPosition(), resp.getLastAllStreamPosition().getPreparePosition());
            return;
        }

        if (resp.hasEvent()) {
            event = ResolvedEvent.fromWire(resp.getEvent());
            return;
        }

        if (resp.getLastStreamPosition() != 0) {
            lastStreamPosition = resp.getLastStreamPosition();
            return;
        }

        firstStreamPosition = resp.getFirstStreamPosition();
    }

    /**
     * If this messages holds the first stream position.
     */
    public boolean hasFirstStreamPosition() {
        return firstStreamPosition != null;
    }

    /**
     * If this messages holds the last stream position.
     */
    public boolean hasLastStreamPosition() {
        return lastStreamPosition != null;
    }

    /**
     * If this messages holds the last $all position.
     */
    public boolean hasLastAllPosition() {
        return lastAllPosition != null;
    }

    /**
     * If this messages holds a resolved event.
     */
    public boolean hasEvent() {
        return event != null;
    }

    /**
     * Returns the first stream position if defined.
     * @throws NullPointerException if not defined.
     */
    public long getFirstStreamPosition() {
        return firstStreamPosition;
    }

    /**
     * Returns the last stream position if defined.
     * @throws NullPointerException if not defined.
     */
    public long getLastStreamPosition() {
        return lastStreamPosition;
    }

    /**
     * Returns the last $all position if defined.
     * @return null is not defined.
     */
    public Position getLastAllPosition() {
        return lastAllPosition;
    }

    /**
     * Returns a resolved event if defined.
     * @return null is not defined.
     */
    public ResolvedEvent getEvent() {
        return event;
    }
}

