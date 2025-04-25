package io.kurrent.dbclient;

import io.kurrent.dbclient.proto.persistentsubscriptions.Persistent;
import io.kurrent.dbclient.proto.shared.Shared;

class CreatePersistentSubscriptionToAll extends AbstractCreatePersistentSubscription<Position, PersistentSubscriptionToAllSettings> {
    private final CreatePersistentSubscriptionToAllOptions options;

    public CreatePersistentSubscriptionToAll(GrpcClient client, String group,
                                             CreatePersistentSubscriptionToAllOptions options) {
        super(client, group, options.getSettings(), options);
        this.options = options;
    }

    @Override
    protected Persistent.CreateReq.Options.Builder createOptions() {
        Persistent.CreateReq.Options.Builder optionsBuilder = Persistent.CreateReq.Options.newBuilder();
        Persistent.CreateReq.AllOptions.Builder allOptionsBuilder = Persistent.CreateReq.AllOptions.newBuilder();
        StreamPosition<Position> position = this.options.getSettings().getStartFrom();

        if (position instanceof StreamPosition.Start) {
            allOptionsBuilder.setStart(Shared.Empty.newBuilder());
        } else if (position instanceof StreamPosition.End) {
            allOptionsBuilder.setEnd(Shared.Empty.newBuilder());
        } else {
            Position pos = position.getPositionOrThrow();
            allOptionsBuilder.setPosition(Persistent.CreateReq.Position.newBuilder()
                    .setCommitPosition(pos.getCommitUnsigned())
                    .setPreparePosition(pos.getPrepareUnsigned()));
        }

        SubscriptionFilter filter = options.getFilter();
        if (filter != null) {
            filter.addToWirePersistentCreateReq(allOptionsBuilder);
        } else {
            allOptionsBuilder.setNoFilter(Shared.Empty.getDefaultInstance());
        }

        optionsBuilder.setAll(allOptionsBuilder);

        return optionsBuilder;
    }
}
