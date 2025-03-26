package io.kurrent.dbclient;

/**
 * Options of the subscribe to stream request.
 */
public class SubscribeToStreamOptions extends OptionsWithStartRevisionAndResolveLinkTosBase<SubscribeToStreamOptions> {
    private SubscribeToStreamOptions() {
        super(OperationKind.Streaming);
    }

    /**
     * Returns options with default values.
     */
    public static SubscribeToStreamOptions get() {
        return new SubscribeToStreamOptions();
    }
}
