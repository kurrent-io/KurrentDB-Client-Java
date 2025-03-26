package io.kurrent.dbclient;

/**
 * @deprecated This class may be removed in future releases. Prefer using appendToStream method with explicit stream state parameter
 */
@Deprecated
class OptionsWithStreamStateBase<T> extends OptionsBase<T> {
    private StreamState streamState;

    protected OptionsWithStreamStateBase() {
        this.streamState = StreamState.any();
    }

    /**
     * @deprecated This method may be removed in future releases. Prefer using appendToStream method with explicit stream state parameter.
     */
    @Deprecated
    StreamState getStreamState() {
        return this.streamState;
    }

    /**
     * Asks the server to check that the stream receiving is at the expected state.
     * @deprecated This method may be removed in future releases. Prefer using appendToStream method with explicit stream state parameter.
     * @param state - expected revision.
     * @return updated options.
     */
    @SuppressWarnings("unchecked")
    @Deprecated
    public T streamState(StreamState state) {
        this.streamState = state;
        return (T) this;
    }


    /**
     * Asks the server to check that the stream receiving is at the given expected revision.
     * @deprecated This method may be removed in future releases. Prefer using appendToStream method with explicit stream state parameter.
     * @param revision - expected revision.
     * @return updated options.
     */
    @Deprecated
    public T streamRevision(long revision) {
        return streamState(StreamState.streamRevision(revision));
    }
}
