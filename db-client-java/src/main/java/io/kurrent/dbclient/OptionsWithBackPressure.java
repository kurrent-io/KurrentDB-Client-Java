package com.eventstore.dbclient;

class OptionsWithBackPressure<T> extends OptionsWithResolveLinkTosBase<T> {
    private int batchSize;
    private float thresholdRatio;

    protected OptionsWithBackPressure(OperationKind kind) {
        super(kind);
        this.batchSize = 512;
        this.thresholdRatio = 0.25f;
    }

    protected OptionsWithBackPressure() {
        this(OperationKind.Streaming);
    }

    int getBatchSize() {
        return batchSize;
    }

    int computeRequestThreshold() {
        return (int)(batchSize * thresholdRatio);
    }

    /**
     * The maximum number of events to read from the server at the time.
     */
    @SuppressWarnings("unchecked")
    public T batchSize(int batchSize) {
        this.batchSize = batchSize;
        return (T)this;
    }

    /**
     * The ratio of the batch size at which more events should be requested from the server.
     */
    @SuppressWarnings("unchecked")
    public T thresholdRatio(float ratio) {
        this.thresholdRatio = ratio;
        return (T)this;
    }
}
