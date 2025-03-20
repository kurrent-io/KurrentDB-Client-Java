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

    public static ReadMessage fromEvent(ResolvedEvent event) {
        ReadMessage msg = new ReadMessage();
        msg.event = event;
        return msg;
    }

    public static ReadMessage fromFirstStreamPosition(long position) {
        ReadMessage msg = new ReadMessage();
        msg.firstStreamPosition = position;
        return msg;
    }

    public static ReadMessage fromLastStreamPosition(long position) {
        ReadMessage msg = new ReadMessage();
        msg.lastStreamPosition = position;
        return msg;
    }

    public static ReadMessage fromLastAllPosition(long commit, long prepare) {
        ReadMessage msg = new ReadMessage();
        msg.lastAllPosition = new Position(commit, prepare);
        return msg;
    }

    ReadMessage() {}

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

