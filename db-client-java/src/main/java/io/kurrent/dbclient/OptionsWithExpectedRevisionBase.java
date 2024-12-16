package io.kurrent.dbclient;

class OptionsWithExpectedRevisionBase<T> extends OptionsBase<T> {
    private ExpectedRevision expectedRevision;

    protected OptionsWithExpectedRevisionBase() {
        this.expectedRevision = ExpectedRevision.any();
    }

    ExpectedRevision getExpectedRevision() {
        return this.expectedRevision;
    }

    /**
     * Asks the server to check that the stream receiving is at the given expected version.

     * @param revision - expected revision.
     * @return updated options.
     */
    @SuppressWarnings("unchecked")
    public T expectedRevision(ExpectedRevision revision) {
        this.expectedRevision = revision;
        return (T) this;
    }


    /**
     * Asks the server to check that the stream receiving is at the given expected version.

     * @param revision - expected revision.
     * @return updated options.
     */
    public T expectedRevision(long revision) {
        return expectedRevision(ExpectedRevision.expectedRevision(revision));
    }
}
