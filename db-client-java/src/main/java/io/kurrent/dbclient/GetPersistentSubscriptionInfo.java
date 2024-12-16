package io.kurrent.dbclient;

import com.eventstore.dbclient.proto.persistentsubscriptions.Persistent;
import com.eventstore.dbclient.proto.persistentsubscriptions.PersistentSubscriptionsGrpc;
import com.eventstore.dbclient.proto.shared.Shared;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.protobuf.ByteString;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static io.kurrent.dbclient.HttpUtils.*;

final class GetPersistentSubscriptionInfo {
    public static CompletableFuture<Optional<PersistentSubscriptionInfo>> execute(GrpcClient client, GetPersistentSubscriptionInfoOptions options, String stream, String groupName) {
        return client.runWithArgs(args -> {
            CompletableFuture<Optional<PersistentSubscriptionInfo>> result = new CompletableFuture<>();

            if (stream.equals("$all") && !args.supportFeature(FeatureFlags.PERSISTENT_SUBSCRIPTION_TO_ALL)) {
                result.completeExceptionally(new UnsupportedFeatureException());
                return result;
            }

            if (args.supportFeature(FeatureFlags.PERSISTENT_SUBSCRIPTION_MANAGEMENT)) {
                Persistent.GetInfoReq.Options.Builder optionsBuilder = Persistent.GetInfoReq.Options.newBuilder();

                if (stream.equals("$all")) {
                    optionsBuilder.setAll(Shared.Empty.getDefaultInstance());
                } else {
                    optionsBuilder.setStreamIdentifier(Shared.StreamIdentifier.newBuilder().setStreamName(ByteString.copyFromUtf8(stream)));
                }

                optionsBuilder.setGroupName(groupName);

                Persistent.GetInfoReq req = Persistent.GetInfoReq.newBuilder()
                        .setOptions(optionsBuilder)
                        .build();

                PersistentSubscriptionsGrpc.PersistentSubscriptionsStub stub =
                        GrpcUtils.configureStub(PersistentSubscriptionsGrpc.newStub(args.getChannel()), client.getSettings(), options);

                CompletableFuture<Optional<PersistentSubscriptionInfo>> tmp = new CompletableFuture<>();
                stub.getInfo(req, GrpcUtils.convertSingleResponse(tmp, resp ->
                    Optional.of(parseInfoFromWire(resp.getSubscriptionInfo()))
                ));

                tmp.whenCompleteAsync((opt, error) -> {
                    if (error instanceof StatusRuntimeException) {
                        StatusRuntimeException status = (StatusRuntimeException) error;

                        if (status.getStatus().getCode() == Status.Code.NOT_FOUND) {
                            result.complete(Optional.empty());
                            return;
                        }
                    }

                    if (error != null) {
                        result.completeExceptionally(error);
                        return;
                    }

                    result.complete(opt);
                });
            } else {
                HttpURLConnection http = args.getHttpConnection(options, client.getSettings(), String.format("/subscriptions/%s/%s/info", urlEncode(stream), urlEncode(groupName)));
                try {
                    http.setRequestMethod("GET");
                    int code = http.getResponseCode();

                    if (code == 404) {
                        result.complete(Optional.empty());
                    } else {
                        Exception error = checkForError(http.getResponseCode());
                        if (error != null) {
                            result.completeExceptionally(error);
                        } else {
                            String content = readContent(http);
                            JsonNode node = getObjectMapper().readTree(content);

                            result.complete(Optional.of(parseSubscriptionInfo(node)));
                        }
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
