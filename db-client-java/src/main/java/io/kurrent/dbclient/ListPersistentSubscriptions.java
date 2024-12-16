package io.kurrent.dbclient;

import com.eventstore.dbclient.proto.persistentsubscriptions.PersistentSubscriptionsGrpc;
import com.eventstore.dbclient.proto.shared.Shared;
import com.fasterxml.jackson.databind.JsonNode;
import com.eventstore.dbclient.proto.persistentsubscriptions.Persistent;
import com.google.protobuf.ByteString;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import static io.kurrent.dbclient.HttpUtils.*;

final class ListPersistentSubscriptions {
    public static <A> CompletableFuture<List<A>> execute(GrpcClient client, ListPersistentSubscriptionsOptions options, String stream, Function<PersistentSubscriptionInfo, A> func) {
        return client.runWithArgs(args -> {
            CompletableFuture<List<A>> result = new CompletableFuture<>();

            if (stream.equals("$all") && !args.supportFeature(FeatureFlags.PERSISTENT_SUBSCRIPTION_TO_ALL)) {
                result.completeExceptionally(new UnsupportedFeatureException());
                return result;
            }

            if (args.supportFeature(FeatureFlags.PERSISTENT_SUBSCRIPTION_MANAGEMENT)) {
                Persistent.ListReq.Options.Builder optionsBuilder = Persistent.ListReq.Options.newBuilder();

                if (stream.equals("")) {
                    optionsBuilder.setListAllSubscriptions(Shared.Empty.getDefaultInstance());
                } else if (stream.equals("$all")) {
                    optionsBuilder.setListForStream(Persistent.ListReq.StreamOption.newBuilder().setAll(Shared.Empty.getDefaultInstance()));
                } else {
                    optionsBuilder.setListForStream(Persistent.ListReq.StreamOption.newBuilder().setStream(Shared.StreamIdentifier.newBuilder().setStreamName(ByteString.copyFromUtf8(stream))));
                }

                Persistent.ListReq req = Persistent.ListReq.newBuilder()
                        .setOptions(optionsBuilder)
                        .build();

                PersistentSubscriptionsGrpc.PersistentSubscriptionsStub stub =
                        GrpcUtils.configureStub(PersistentSubscriptionsGrpc.newStub(args.getChannel()), client.getSettings(), options);

                stub.list(req, GrpcUtils.convertSingleResponse(result, resp -> {
                    List<A> infos = new ArrayList<>();

                    for (Persistent.SubscriptionInfo wire : resp.getSubscriptionsList()) {
                        infos.add(func.apply(parseInfoFromWire(wire)));
                    }

                    return infos;
                }));
            } else {
                String suffix = "";

                if (!stream.equals("")) {
                    suffix = String.format("/%s", urlEncode(stream));
                }

                HttpURLConnection http = args.getHttpConnection(options, client.getSettings(), String.format("/subscriptions%s", suffix));
                try {
                    http.setRequestMethod("GET");

                    Exception error = checkForError(http.getResponseCode());
                    if (error != null) {
                        result.completeExceptionally(error);
                    } else {
                        String content = readContent(http);
                        List<A> ps = new ArrayList<>();

                        for (JsonNode node : getObjectMapper().readTree(content)) {
                            ps.add(func.apply(parseSubscriptionInfo(node)));
                        }
                        result.complete(ps);
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
