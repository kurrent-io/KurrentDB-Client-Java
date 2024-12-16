package io.kurrent.dbclient;

import io.kurrent.dbclient.proto.persistentsubscriptions.Persistent;
import io.kurrent.dbclient.proto.persistentsubscriptions.PersistentSubscriptionsGrpc;
import io.kurrent.dbclient.proto.shared.Shared;
import com.google.protobuf.ByteString;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.concurrent.CompletableFuture;

import static io.kurrent.dbclient.HttpUtils.*;

final class ReplayParkedMessages {
    @SuppressWarnings("unchecked")
    public static CompletableFuture execute(GrpcClient client, ReplayParkedMessagesOptions options, String stream, String groupName) {
        return client.runWithArgs(args -> {
           CompletableFuture result = new CompletableFuture();

            if (stream.equals("$all") && !args.supportFeature(FeatureFlags.PERSISTENT_SUBSCRIPTION_TO_ALL)) {
                result.completeExceptionally(new UnsupportedFeatureException());
                return result;
            }

            if (args.supportFeature(FeatureFlags.PERSISTENT_SUBSCRIPTION_MANAGEMENT)) {
               Persistent.ReplayParkedReq.Options.Builder optionBuilder = Persistent.ReplayParkedReq.Options.newBuilder();

               if (stream.equals("$all")) {
                    optionBuilder.setAll(Shared.Empty.getDefaultInstance());
               } else {
                    optionBuilder.setStreamIdentifier(Shared.StreamIdentifier.newBuilder().setStreamName(ByteString.copyFromUtf8(stream)));
               }

               optionBuilder.setGroupName(groupName);
               if (options.getStopAt() != null) {
                   optionBuilder.setStopAt(options.getStopAt());
               } else {
                   optionBuilder.setNoLimit(Shared.Empty.getDefaultInstance());
               }

               Persistent.ReplayParkedReq req = Persistent.ReplayParkedReq.newBuilder()
                       .setOptions(optionBuilder)
                       .build();

               PersistentSubscriptionsGrpc.PersistentSubscriptionsStub stub =
                       GrpcUtils.configureStub(PersistentSubscriptionsGrpc.newStub(args.getChannel()), client.getSettings(), options);

               stub.replayParked(req, GrpcUtils.convertSingleResponse(result, resp -> 42 ));
           } else {
               String query;
               if (options.getStopAt() != null) {
                   query = String.format("?stopAt=%s", options.getStopAt());
               } else {
                   query = "";
               }

               HttpURLConnection http = args.getHttpConnection(options, client.getSettings(), String.format("/subscriptions/%s/%s/replayParked%s", urlEncode(stream), urlEncode(groupName), query));

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
