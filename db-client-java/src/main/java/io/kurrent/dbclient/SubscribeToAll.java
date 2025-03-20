package io.kurrent.dbclient;

import io.kurrent.dbclient.proto.shared.Shared;
import io.kurrent.dbclient.proto.streams.StreamsOuterClass;

class SubscribeToAll extends AbstractRegularSubscription {
    private final SubscribeToAllOptions options;

    public SubscribeToAll(GrpcClient client, SubscriptionListener listener, SubscribeToAllOptions options) {
        super(client, options);

        this.options = options;
        this.listener = listener;
    }

    @Override
    protected StreamsOuterClass.ReadReq.Options.Builder createOptions() {
        StreamsOuterClass.ReadReq.Options.AllOptions.Builder allOptions = StreamsOuterClass.ReadReq.Options.AllOptions.newBuilder();

        if (this.options.getPosition().isEnd()) {
            allOptions.setEnd(Shared.Empty.getDefaultInstance());
        } else if (this.options.getPosition().isStart()) {
            allOptions.setStart(Shared.Empty.getDefaultInstance());
        } else {
            StreamPosition.Position<Position> position = (StreamPosition.Position<Position>) this.options.getPosition();
            allOptions.setPosition(StreamsOuterClass.ReadReq.Options.Position.newBuilder()
                    .setCommitPosition(position.getPositionOrThrow().getCommitUnsigned())
                    .setPreparePosition(position.getPositionOrThrow().getPrepareUnsigned()));
        }
        StreamsOuterClass.ReadReq.Options.Builder options =
                defaultSubscribeOptions.clone()
                        .setResolveLinks(this.options.shouldResolveLinkTos())
                        .setAll(allOptions);

        if (this.options.getFilter() != null) {
            this.options.getFilter().addToWireStreamsReadReq(options);
            this.checkpointer = this.options.getFilter().getCheckpointer();
        } else {
            options.setNoFilter(Shared.Empty.getDefaultInstance());
        }

        return options;
    }
}
