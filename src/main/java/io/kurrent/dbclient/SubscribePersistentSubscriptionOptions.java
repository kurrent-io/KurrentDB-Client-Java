package io.kurrent.dbclient;

/**
 * Options of the subscribe persistent subscription request.
 */
public class SubscribePersistentSubscriptionOptions extends OptionsBase<SubscribePersistentSubscriptionOptions> {
    private int bufferSize;

    private SubscribePersistentSubscriptionOptions() {
        super(OperationKind.Streaming);
        this.bufferSize = 10;
    }

    /**
     * Returns options with default values.
     */
    public static SubscribePersistentSubscriptionOptions get() {
        return new SubscribePersistentSubscriptionOptions();
    }

    int getBufferSize() {
        return bufferSize;
    }

    /**
     * Persistent subscription's buffer size.
     */
    public SubscribePersistentSubscriptionOptions bufferSize(int value) {
        bufferSize = value;
        return this;
    }
}
