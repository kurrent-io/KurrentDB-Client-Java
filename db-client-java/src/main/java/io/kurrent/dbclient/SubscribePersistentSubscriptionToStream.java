package io.kurrent.dbclient;

import io.kurrent.dbclient.proto.persistentsubscriptions.Persistent;
import io.kurrent.dbclient.proto.shared.Shared;
import com.google.protobuf.ByteString;

class SubscribePersistentSubscriptionToStream extends AbstractSubscribePersistentSubscription {
    private final String stream;

    public SubscribePersistentSubscriptionToStream(GrpcClient connection, String stream, String group,
                                                   SubscribePersistentSubscriptionOptions options,
                                                   PersistentSubscriptionListener listener) {
        super(connection, group, options, listener);

        this.stream = stream;
    }

    @Override
    protected Persistent.ReadReq.Options.Builder createOptions() {
        Shared.StreamIdentifier streamIdentifier =
                Shared.StreamIdentifier.newBuilder()
                        .setStreamName(ByteString.copyFromUtf8(stream))
                        .build();

        return defaultReadOptions.clone()
                .setStreamIdentifier(streamIdentifier);
    }
}
