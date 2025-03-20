package io.kurrent.dbclient;

import io.kurrent.dbclient.proto.persistentsubscriptions.Persistent;
import io.kurrent.dbclient.proto.shared.Shared;

class DeletePersistentSubscriptionToAll extends AbstractDeletePersistentSubscription {
    public DeletePersistentSubscriptionToAll(GrpcClient client, String group,
                                             DeletePersistentSubscriptionOptions options) {
        super(client, group, options);
    }

    @Override
    protected Persistent.DeleteReq.Options.Builder createOptions() {
        return Persistent.DeleteReq.Options.newBuilder()
                .setAll(Shared.Empty.newBuilder());
    }
}
