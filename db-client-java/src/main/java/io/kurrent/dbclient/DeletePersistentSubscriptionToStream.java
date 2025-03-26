package io.kurrent.dbclient;

import io.kurrent.dbclient.proto.persistentsubscriptions.Persistent;
import io.kurrent.dbclient.proto.shared.Shared;
import com.google.protobuf.ByteString;

class DeletePersistentSubscriptionToStream extends AbstractDeletePersistentSubscription {
    private String stream;

    public DeletePersistentSubscriptionToStream(GrpcClient client, String stream, String group,
                                                DeletePersistentSubscriptionOptions options) {
        super(client, group, options);
        this.stream = stream;
    }

    @Override
    protected Persistent.DeleteReq.Options.Builder createOptions() {
        Shared.StreamIdentifier.Builder streamIdentifier =
                Shared.StreamIdentifier.newBuilder()
                        .setStreamName(ByteString.copyFromUtf8(stream));

        return Persistent.DeleteReq.Options.newBuilder()
                .setStreamIdentifier(streamIdentifier);
    }
}