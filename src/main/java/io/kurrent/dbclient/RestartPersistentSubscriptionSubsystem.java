package io.kurrent.dbclient;

import io.kurrent.dbclient.proto.persistentsubscriptions.PersistentSubscriptionsGrpc;
import io.kurrent.dbclient.proto.shared.Shared;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.concurrent.CompletableFuture;

import static io.kurrent.dbclient.HttpUtils.checkForError;

class RestartPersistentSubscriptionSubsystem {
    @SuppressWarnings("unchecked")
    public static CompletableFuture execute(GrpcClient client, RestartPersistentSubscriptionSubsystemOptions options) {
        return client.runWithArgs(args -> {
            CompletableFuture result = new CompletableFuture();

            if (args.supportFeature(FeatureFlags.PERSISTENT_SUBSCRIPTION_MANAGEMENT)) {
                PersistentSubscriptionsGrpc.PersistentSubscriptionsStub stub =
                        GrpcUtils.configureStub(PersistentSubscriptionsGrpc.newStub(args.getChannel()), client.getSettings(), options);

                stub.restartSubsystem(Shared.Empty.getDefaultInstance(), GrpcUtils.convertSingleResponse(result, resp -> 42));
            } else {
                HttpURLConnection http = args.getHttpConnection(options, client.getSettings(), "/subscriptions/restart");

                try {
                    http.setDoOutput(true);
                    http.setRequestMethod("POST");
                    http.setFixedLengthStreamingMode(0);

                    Exception error = checkForError(http.getResponseCode());
                    if (error != null) {
                        result.completeExceptionally(error);
                    } else {
                        result.complete(42);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } finally {
                    http.disconnect();
                }
            }

            return result;
        });
    }
}
