package io.kurrent.dbclient;

import com.eventstore.dbclient.proto.persistentsubscriptions.Persistent;
import com.eventstore.dbclient.proto.shared.Shared;

class UpdatePersistentSubscriptionToAll extends AbstractUpdatePersistentSubscription {
    private final UpdatePersistentSubscriptionToAllOptions options;
    public UpdatePersistentSubscriptionToAll(GrpcClient connection, String group,
                                             UpdatePersistentSubscriptionToAllOptions options) {
        super(connection, group, options.getSettings(), options);

        this.options = options;
    }

    @Override
    protected Persistent.UpdateReq.Options.Builder createOptions() {
        Persistent.UpdateReq.Options.Builder optionsBuilder = Persistent.UpdateReq.Options.newBuilder();
        Persistent.UpdateReq.AllOptions.Builder allOptionsBuilder = Persistent.UpdateReq.AllOptions.newBuilder();

        StreamPosition<Position> startFrom = options.getSettings().getStartFrom();

        if (startFrom instanceof StreamPosition.Start) {
            allOptionsBuilder.setStart(Shared.Empty.newBuilder());
        } else if (startFrom instanceof StreamPosition.End) {
            allOptionsBuilder.setEnd(Shared.Empty.newBuilder());
        } else {
            Position position = startFrom.getPositionOrThrow();
            allOptionsBuilder.setPosition(Persistent.UpdateReq.Position.newBuilder()
                    .setCommitPosition(position.getCommitUnsigned())
                    .setPreparePosition(position.getPrepareUnsigned()));
        }

        optionsBuilder.setAll(allOptionsBuilder);

        return optionsBuilder;
    }
}
