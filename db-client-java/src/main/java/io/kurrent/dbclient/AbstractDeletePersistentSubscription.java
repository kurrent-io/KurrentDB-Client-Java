package io.kurrent.dbclient;

import io.kurrent.dbclient.proto.persistentsubscriptions.Persistent;
import io.kurrent.dbclient.proto.persistentsubscriptions.PersistentSubscriptionsGrpc;

import java.util.concurrent.CompletableFuture;

abstract class AbstractDeletePersistentSubscription {
    private final GrpcClient client;
    private final String group;
    private final DeletePersistentSubscriptionOptions options;

    public AbstractDeletePersistentSubscription(GrpcClient client, String group, DeletePersistentSubscriptionOptions options) {
        this.client = client;
        this.group = group;
        this.options = options;
    }

    protected abstract Persistent.DeleteReq.Options.Builder createOptions();

    @SuppressWarnings("unchecked")
    public CompletableFuture execute() {
        return this.client.runWithArgs(args -> {
            CompletableFuture result = new CompletableFuture();
            PersistentSubscriptionsGrpc.PersistentSubscriptionsStub client =
                    GrpcUtils.configureStub(PersistentSubscriptionsGrpc.newStub(args.getChannel()), this.client.getSettings(), this.options);

            Persistent.DeleteReq req = Persistent.DeleteReq.newBuilder()
                    .setOptions(createOptions()
                            .setGroupName(group))
                    .build();

            if (req.getOptions().hasAll() && !args.supportFeature(FeatureFlags.PERSISTENT_SUBSCRIPTION_TO_ALL)) {
                result.completeExceptionally(new UnsupportedFeatureException());
            } else {
                client.delete(req, GrpcUtils.convertSingleResponse(result));
            }

            return result;
        });
    }
}
