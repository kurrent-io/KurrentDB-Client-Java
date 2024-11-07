package com.eventstore.dbclient;

import com.eventstore.dbclient.proto.shared.Shared;
import com.eventstore.dbclient.proto.streams.StreamsOuterClass;

class ReadAll extends AbstractRead {
    private final ReadAllOptions options;

    public ReadAll(GrpcClient client, ReadAllOptions options) {
        super(client, options);

        this.options = options;
    }

    @Override
    public StreamsOuterClass.ReadReq.Options.Builder createOptions() {
        StreamsOuterClass.ReadReq.Options.AllOptions.Builder optionsOrBuilder =
                StreamsOuterClass.ReadReq.Options.AllOptions.newBuilder();

        if (this.options.getPosition().isEnd()) {
            optionsOrBuilder.setEnd(Shared.Empty.getDefaultInstance());
        } else if (this.options.getPosition().isStart()) {
            optionsOrBuilder.setStart(Shared.Empty.getDefaultInstance());
        } else {
            StreamPosition.Position<Position> position = (StreamPosition.Position<Position>) this.options.getPosition();
            optionsOrBuilder.setPosition(StreamsOuterClass.ReadReq.Options.Position.newBuilder()
                    .setCommitPosition(position.getPositionOrThrow().getCommitUnsigned())
                    .setPreparePosition(position.getPositionOrThrow().getPrepareUnsigned()));
        }

        StreamsOuterClass.ReadReq.Options.Builder builder = defaultReadOptions.clone()
                .setAll(optionsOrBuilder)
                .setResolveLinks(this.options.shouldResolveLinkTos())
                .setControlOption(StreamsOuterClass.ReadReq.Options.ControlOption.newBuilder().setCompatibility(1))
                .setCount(this.options.getMaxCount())
                //.setNoFilter(Shared.Empty.getDefaultInstance())
                .setReadDirection(this.options.getDirection() == Direction.Forwards ?
                        StreamsOuterClass.ReadReq.Options.ReadDirection.Forwards :
                        StreamsOuterClass.ReadReq.Options.ReadDirection.Backwards);

        if (this.options.getFilter() != null) {
            this.options.getFilter().addToWireStreamsReadReq(builder);
        } else {
            builder.setNoFilter(Shared.Empty.getDefaultInstance());
        }

        return builder;
    }
}
