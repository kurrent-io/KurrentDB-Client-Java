package io.kurrent.dbclient;

import com.google.protobuf.ByteString;
import io.grpc.Metadata;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import io.kurrentdb.protocol.DynamicValue;
import io.kurrentdb.protocol.streams.v2.AppendRecord;
import io.kurrentdb.protocol.streams.v2.MultiStreamAppendResponse;
import io.kurrentdb.protocol.streams.v2.StreamsServiceGrpc;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

class MultiStreamAppend {
    private final GrpcClient client;
    private final Iterator<AppendStreamRequest> requests;

    public MultiStreamAppend(GrpcClient client, Iterator<AppendStreamRequest> requests) {
        this.client = client;
        this.requests = requests;
    }

    public CompletableFuture<MultiAppendWriteResult> execute() {
        return this.client.runWithArgs(this::append);
    }

    private CompletableFuture<MultiAppendWriteResult> append(WorkItemArgs args) {
        CompletableFuture<MultiAppendWriteResult> result = new CompletableFuture<>();

        if (!args.supportFeature(FeatureFlags.MULTI_STREAM_APPEND)) {
            result.completeExceptionally(new UnsupportedOperationException("Multi-stream append is not supported by the server"));
            return result;
        }

        StreamsServiceGrpc.StreamsServiceStub client =  GrpcUtils.configureStub(StreamsServiceGrpc.newStub(args.getChannel()), this.client.getSettings(), new OptionsBase<>(), null, false);
        StreamObserver<io.kurrentdb.protocol.streams.v2.AppendStreamRequest> requestStream = client.multiStreamAppendSession(GrpcUtils.convertSingleResponse(result, this::onResponse));

       try {
           while (this.requests.hasNext()) {
                AppendStreamRequest request = this.requests.next();
                io.kurrentdb.protocol.streams.v2.AppendStreamRequest.Builder builder = io.kurrentdb.protocol.streams.v2.AppendStreamRequest.newBuilder()
                        .setExpectedRevision(request.getExpectedState().toRawLong())
                        .setStream(request.getStreamName());

               while (request.getEvents().hasNext()) {
                     EventData event = request.getEvents().next();
                     AppendRecord.Builder recordBuilder = AppendRecord.newBuilder()
                             .setData(ByteString.copyFrom(event.getEventData()))
                             .setRecordId(event.getEventId().toString())
                             .putProperties(SystemMetadataKeys.DATA_FORMAT, DynamicValue
                                     .newBuilder()
                                     .setStringValue(ContentTypeMapper.toSchemaDataFormat(event.getContentType()))
                                     .build())
                             .putProperties(SystemMetadataKeys.SCHEMA_NAME, DynamicValue
                                     .newBuilder()
                                     .setStringValue(event.getEventType())
                                     .build());

                     if (event.getUserMetadata() != null) {
                         Map<String, DynamicValue> userMetadataProperties = DynamicValueMapper.mapJsonToDynamicValueMap(event.getUserMetadata());
                         recordBuilder.putAllProperties(userMetadataProperties);
                     }

                     builder.addRecords(recordBuilder.build());
               }

               requestStream.onNext(builder.build());
           }

           requestStream.onCompleted();
       } catch (StatusRuntimeException e) {
           String leaderHost = e.getTrailers().get(Metadata.Key.of("leader-endpoint-host", Metadata.ASCII_STRING_MARSHALLER));
           String leaderPort = e.getTrailers().get(Metadata.Key.of("leader-endpoint-port", Metadata.ASCII_STRING_MARSHALLER));

           if (leaderHost != null && leaderPort != null) {
               NotLeaderException reason = new NotLeaderException(leaderHost, Integer.valueOf(leaderPort));
               requestStream.onError(reason);
               result.completeExceptionally(reason);
           } else {
               requestStream.onError(e);
               result.completeExceptionally(e);
           }
       } catch (RuntimeException e) {
           requestStream.onError(e);
           result.completeExceptionally(e);
       }

        return result;
    }

    public MultiAppendWriteResult onResponse(MultiStreamAppendResponse response) {
        List<AppendStreamFailure> failures = null;
        List<AppendStreamSuccess> successes = null;

        if (response.hasFailure()) {
            failures = new ArrayList<>(response.getFailure().getOutputCount());

            for (io.kurrentdb.protocol.streams.v2.AppendStreamFailure failure : response.getFailure().getOutputList()) {
                failures.add(new AppendStreamFailure(failure));
            }
        } else {
            successes = new ArrayList<>(response.getSuccess().getOutputCount());

            for (io.kurrentdb.protocol.streams.v2.AppendStreamSuccess success : response.getSuccess().getOutputList()) {
                successes.add(new AppendStreamSuccess(success));
            }
        }

        return new MultiAppendWriteResult(successes, failures);
    }
}
