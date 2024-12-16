package io.kurrent.dbclient;

import com.eventstore.dbclient.proto.persistentsubscriptions.Persistent;
import com.eventstore.dbclient.proto.shared.Shared;

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
