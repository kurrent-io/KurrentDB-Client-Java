package io.kurrent.dbclient;

/**
 * Options of a subscription to $all request.
 */
public class SubscribeToAllOptions extends OptionsWithPositionAndResolveLinkTosBase<SubscribeToAllOptions> {
    private SubscriptionFilter filter;

    private SubscribeToAllOptions() {
        super(OperationKind.Streaming);
    }

    /**
     * Returns options with default values.
     */
    public static SubscribeToAllOptions get() {
        return new SubscribeToAllOptions();
    }

    SubscriptionFilter getFilter() {
        return filter;
    }

    /**
     * Applies a server-side filter to determine if an event of the subscription should be yielded.
     */
    public SubscribeToAllOptions filter(SubscriptionFilter filter) {
        this.filter = filter;
        return this;
    }
}
