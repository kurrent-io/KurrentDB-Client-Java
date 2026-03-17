package io.kurrent.dbclient;

/**
 * Represents a consistency check to be evaluated before committing an
 * {@link KurrentDBClient#appendRecords} operation.
 * Checks are decoupled from writes: a check can reference any stream,
 * whether or not the request writes to it.
 */
public abstract class ConsistencyCheck {

    ConsistencyCheck() {}

    /**
     * A check that asserts a stream is at a specific revision or lifecycle state before commit.
     */
    public static class StreamStateCheck extends ConsistencyCheck {
        private final String stream;
        private final StreamState expectedState;

        /**
         * Creates a new StreamStateCheck.
         *
         * @param stream the stream name to check.
         * @param expectedState the expected state of the stream (revision number or state constant).
         */
        public StreamStateCheck(String stream, StreamState expectedState) {
            this.stream = stream;
            this.expectedState = expectedState;
        }

        /**
         * Returns the stream name to check.
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
    }
}
