package io.kurrent.dbclient;

import com.eventstore.dbclient.proto.shared.Shared;
import com.eventstore.dbclient.proto.streams.StreamsOuterClass;

class SubscribeToStream extends AbstractRegularSubscription {
    private final SubscribeToStreamOptions options;
    private final String streamName;

    public SubscribeToStream(GrpcClient client, String streamName, SubscriptionListener listener, SubscribeToStreamOptions options) {
        super(client, options);

        this.streamName = streamName;
        this.options = options;

        this.listener = listener;
    }


    @Override
    protected StreamsOuterClass.ReadReq.Options.Builder createOptions() {
        return defaultSubscribeOptions.clone()
                .setResolveLinks(this.options.shouldResolveLinkTos())
                .setNoFilter(Shared.Empty.getDefaultInstance())
                .setStream(GrpcUtils.toStreamOptions(this.streamName, this.options.getStartingRevision()));
    }
}
