package com.eventstore.dbclient;

class OptionsWithStreamStateBase<T> extends OptionsBase<T> {
    private StreamState streamState;

    protected OptionsWithStreamStateBase() {
        this.streamState = StreamState.any();
    }

    StreamState getStreamState() {
        return this.streamState;
    }

    /**
     * Asks the server to check that the stream receiving is at the expected state.

     * @param state - expected revision.
     * @return updated options.
     */
    @SuppressWarnings("unchecked")
    public T streamState(StreamState state) {
        this.streamState = state;
        return (T) this;
    }


    /**
     * Asks the server to check that the stream receiving is at the given expected revision.

     * @param revision - expected revision.
     * @return updated options.
     */
    public T streamRevision(long revision) {
        return streamState(StreamState.streamRevision(revision));
    }
}
