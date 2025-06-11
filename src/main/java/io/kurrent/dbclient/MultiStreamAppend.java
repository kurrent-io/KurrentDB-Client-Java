package io.kurrent.dbclient;

import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import io.kurrentdb.v2.AppendRecord;
import io.kurrentdb.v2.MultiStreamAppendResponse;
import io.kurrentdb.v2.StreamsServiceGrpc;
import kurrentdb.protobuf.DynamicValueOuterClass;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

class MultiStreamAppend {
    private final GrpcClient client;
    private final Iterator<AppendStreamRequest> requests;

    public MultiStreamAppend(GrpcClient client, Iterator<AppendStreamRequest> requests) {
        this.client = client;
        this.requests = requests;
    }

    public CompletableFuture<MultiAppendWriteResult> execute() {
        return this.client.run(this::append);
    }

    private CompletableFuture<MultiAppendWriteResult> append(ManagedChannel channel) {
        CompletableFuture<MultiAppendWriteResult> result = new CompletableFuture<>();
        StreamsServiceGrpc.StreamsServiceStub client =  GrpcUtils.configureStub(StreamsServiceGrpc.newStub(channel), this.client.getSettings(), new OptionsBase<>());
        StreamObserver<io.kurrentdb.v2.AppendStreamRequest> requestStream = client.multiStreamAppendSession(GrpcUtils.convertSingleResponse(result, this::onResponse));

       try {
           while (this.requests.hasNext()) {
                AppendStreamRequest request = this.requests.next();
                io.kurrentdb.v2.AppendStreamRequest.Builder builder = io.kurrentdb.v2.AppendStreamRequest.newBuilder()
                        .setStream(request.getStreamName());

               while (request.getEvents().hasNext()) {
                     EventData event = request.getEvents().next();
                     builder.addRecords(AppendRecord.newBuilder()
                             .setData(ByteString.copyFrom(event.getEventData()))
                                     .setRecordId(event.getEventId().toString())
                                     .putProperties(SystemMetadataKeys.CONTENT_TYPE, DynamicValueOuterClass
                                             .DynamicValue
                                             .newBuilder()
                                             .setStringValue(event.getContentType())
                                             .build())
                                     .putProperties(SystemMetadataKeys.TYPE, DynamicValueOuterClass
                                             .DynamicValue
                                             .newBuilder()
                                             .setStringValue(event.getEventType())
                                             .build())
                                     .build());
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

            for (io.kurrentdb.v2.AppendStreamFailure failure : response.getFailure().getOutputList()) {
                failures.add(new AppendStreamFailure(failure));
            }
        } else {
            successes = new ArrayList<>(response.getSuccess().getOutputCount());

            for (io.kurrentdb.v2.AppendStreamSuccess success : response.getSuccess().getOutputList()) {
                successes.add(new AppendStreamSuccess(success));
            }
        }

        return new MultiAppendWriteResult(successes, failures);
    }
}
