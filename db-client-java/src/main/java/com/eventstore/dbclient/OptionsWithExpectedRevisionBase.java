package com.eventstore.dbclient;

class OptionsWithExpectedRevisionBase<T> extends OptionsBase<T> {
    private StreamState expectedRevision;

    protected OptionsWithExpectedRevisionBase() {
        this.expectedRevision = StreamState.any();
    }

    StreamState getExpectedRevision() {
        return this.expectedRevision;
    }

    /**
     * Asks the server to check that the stream receiving is at the given expected version.

     * @param revision - expected revision.
     * @return updated options.
     */
    @SuppressWarnings("unchecked")
    public T expectedRevision(StreamState revision) {
        this.expectedRevision = revision;
        return (T) this;
    }


    /**
     * Asks the server to check that the stream receiving is at the given expected version.

     * @param revision - expected revision.
     * @return updated options.
     */
    public T expectedRevision(long revision) {
        return expectedRevision(StreamState.streamRevision(revision));
    }
}
