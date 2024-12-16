package io.kurrent.dbclient;

import com.eventstore.dbclient.proto.persistentsubscriptions.Persistent;
import com.eventstore.dbclient.proto.shared.Shared;

class SubscribePersistentSubscriptionToAll extends AbstractSubscribePersistentSubscription {
    public SubscribePersistentSubscriptionToAll(GrpcClient connection, String group,
                                                SubscribePersistentSubscriptionOptions options,
                                                PersistentSubscriptionListener listener) {
        super(connection, group, options, listener);
    }

    @Override
    protected Persistent.ReadReq.Options.Builder createOptions() {
        return defaultReadOptions.clone()
                .setAll(Shared.Empty.newBuilder());
    }
}
