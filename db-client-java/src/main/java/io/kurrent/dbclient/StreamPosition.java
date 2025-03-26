package io.kurrent.dbclient;

import java.util.Optional;

/**
 * Represents a logical position in a regular stream or $all stream.
 * @param <A> could be a revision number or transaction log position.
 */
public class StreamPosition<A> {
    /**
     * Represents the beginning of a stream.
     */
    public static <A> StreamPosition<A> start() {
        return new Start<>();
    }

    /**
     * Represents the end of a stream.
     */
    public static <A> StreamPosition<A> end() {
        return new End<>();
    }

    /**
     * Represents a specific position.
     */
    public static <A> StreamPosition<A> position(A position) {
        return new Position<>(position);
    }

    StreamPosition(){}

    /**
     * Checks if it's the beginning of the stream.
     */
    public boolean isStart() {
        return this instanceof Start;
    }

    /**
     * Checks if it's the end of the stream.
     */
    public boolean isEnd() {
        return this instanceof End;
    }

    /**
     * Checks it's a specific position and returns the value.
     */
    public Optional<A> getPosition() {
        if (this instanceof StreamPosition.Position) {
            StreamPosition.Position<A> pos = (StreamPosition.Position<A>) this;
            return Optional.of(pos.getValue());
        }

        return Optional.empty();
    }

    /**
     * Checks if it's a specific position and returns the value.
     */
    public A getPositionOrThrow() {
        return getPosition().get();
    }

    final static class Start<A> extends StreamPosition<A> {
        @Override
        public String toString() {
            return "Start";
        }
    }

    final static class End<A> extends StreamPosition<A> {
        @Override
        public String toString() {
            return "End";
        }
    }

    final static class Position<A> extends StreamPosition<A> {
        private final A position;

        public Position(A position) {
            this.position = position;
        }

        public A getValue() {
            return position;
        }

        @Override
        public String toString() {
            return position.toString();
        }
    }
}
