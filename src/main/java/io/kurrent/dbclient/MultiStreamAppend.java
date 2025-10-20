package io.kurrent.dbclient;

import com.google.protobuf.ByteString;
import com.google.protobuf.Value;
import com.google.rpc.ErrorInfo;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import io.grpc.stub.ClientResponseObserver;
import io.grpc.stub.ClientCallStreamObserver;
import io.kurrentdb.protocol.v2.streams.AppendRecord;
import io.kurrentdb.protocol.v2.streams.AppendRequest;
import io.kurrentdb.protocol.v2.streams.AppendSessionResponse;
import io.kurrentdb.protocol.v2.streams.SchemaInfo;
import io.kurrentdb.protocol.v2.streams.StreamsServiceGrpc;

import io.kurrentdb.protocol.v2.streams.errors.*;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.google.protobuf.Any;
import io.grpc.protobuf.StatusProto;

class MultiStreamAppend {
    private final GrpcClient client;
    private final Iterator<AppendStreamRequest> requests;

    public MultiStreamAppend(GrpcClient client, Iterator<AppendStreamRequest> requests) {
        this.client = client;
        this.requests = requests;
    }

    public CompletableFuture<MultiStreamAppendResponse> execute() {
        return this.client.runWithArgs(args -> ClientTelemetry.traceMultiStreamAppend(
                this::append,
                args,
                this.requests,
                this.client.getSettings()));
    }

    private CompletableFuture<MultiStreamAppendResponse> append(WorkItemArgs args, Iterator<AppendStreamRequest> requests) {
        CompletableFuture<MultiStreamAppendResponse> result = new CompletableFuture<>();

        if (!args.supportFeature(FeatureFlags.MULTI_STREAM_APPEND)) {
            result.completeExceptionally(new UnsupportedOperationException("Multi-stream append is not supported by the server"));
            return result;
        }

        StreamsServiceGrpc.StreamsServiceStub client = GrpcUtils.configureStub(StreamsServiceGrpc.newStub(args.getChannel()), this.client.getSettings(), new OptionsBase<>(), null, false);
        StreamObserver<AppendRequest> requestStream = client.appendSession(new MultiStreamAppendObserver(result));

        try {
            while (requests.hasNext()) {
                AppendStreamRequest request = requests.next();
                AppendRequest.Builder builder = AppendRequest.newBuilder()
                        .setExpectedRevision(request.getExpectedState().toRawLong())
                        .setStream(request.getStreamName());

                while (request.getEvents().hasNext()) {
                    EventData event = request.getEvents().next();
                    AppendRecord.Builder recordBuilder = AppendRecord.newBuilder()
                            .setData(ByteString.copyFrom(event.getEventData()))
                            .setRecordId(event.getEventId().toString())
                            .setSchema(
                                    SchemaInfo.newBuilder()
                                            .setFormat(ContentTypeMapper.toSchemaDataFormat(event.getContentType()))
                                            .setName(event.getEventType())
                            );

                    if (event.getUserMetadata() != null) {
                        Map<String, Value> userMetadataProperties = DynamicValueMapper.mapJsonToValueMap(event.getUserMetadata());
                        recordBuilder.putAllProperties(userMetadataProperties);
                    }
                    builder.addRecords(recordBuilder.build());
                }

                requestStream.onNext(builder.build());
            }

            requestStream.onCompleted();
        } catch (RuntimeException e) {
            result.completeExceptionally(e);
        }

        return result;
    }

    private MultiStreamAppendResponse onResponse(AppendSessionResponse response) {
        List<AppendResponse> results = new java.util.ArrayList<>(response.getOutputCount());

        for (io.kurrentdb.protocol.v2.streams.AppendResponse output : response.getOutputList()) {
            results.add(new AppendResponse(output.getStream(), output.getStreamRevision()));
        }

        return new MultiStreamAppendResponse(response.getPosition(), results);
    }

    private class MultiStreamAppendObserver implements ClientResponseObserver<AppendRequest, AppendSessionResponse> {
        private final CompletableFuture<MultiStreamAppendResponse> result;

        public MultiStreamAppendObserver(CompletableFuture<MultiStreamAppendResponse> result) {
            this.result = result;
        }

        @Override
        public void beforeStart(ClientCallStreamObserver<AppendRequest> requestStream) {
        }

        @Override
        public void onNext(AppendSessionResponse response) {
            try {
                MultiStreamAppendResponse converted = onResponse(response);
                result.complete(converted);
            } catch (Throwable e) {
                result.completeExceptionally(e);
            }
        }

        @Override
        public void onError(Throwable t) {
            if (GrpcUtils.handleNotLeaderError(t, result)) return;

            if (t instanceof StatusRuntimeException) {
                StatusRuntimeException e = (StatusRuntimeException) t;
                com.google.rpc.Status status = StatusProto.fromThrowable(e);

                if (status != null && status.getDetailsCount() > 0) {
                    for (Any d : status.getDetailsList()) {
                        try {
                            if (d.is(StreamRevisionConflictErrorDetails.class)) {
                                StreamRevisionConflictErrorDetails details = d.unpack(StreamRevisionConflictErrorDetails.class);
                                StreamState expected = StreamState.fromRawLong(details.getExpectedRevision());
                                StreamState actual = StreamState.fromRawLong(details.getActualRevision());
                                String stream = details.getStream();
                                result.completeExceptionally(new WrongExpectedVersionException(stream, expected, actual));
                                return;
                            } else if (d.is(StreamDeletedErrorDetails.class)) {
                                StreamDeletedErrorDetails details = d.unpack(StreamDeletedErrorDetails.class);
                                result.completeExceptionally(new StreamDeletedException(details.getStream()));
                                return;
                            } else if (d.is(StreamTombstonedErrorDetails.class)) {
                                StreamTombstonedErrorDetails details = d.unpack(StreamTombstonedErrorDetails.class);
                                result.completeExceptionally(new StreamTombstonedException(details.getStream()));
                                return;
                            } else if (d.is(AppendRecordSizeExceededErrorDetails.class)) {
                                AppendRecordSizeExceededErrorDetails details = d.unpack(AppendRecordSizeExceededErrorDetails.class);
                                result.completeExceptionally(new RecordSizeExceededException(details.getStream(), details.getRecordId(), details.getSize(), details.getMaxSize()));
                                return;
                            } else if (d.is(AppendTransactionSizeExceededErrorDetails.class)) {
                                AppendTransactionSizeExceededErrorDetails details = d.unpack(AppendTransactionSizeExceededErrorDetails.class);
                                result.completeExceptionally(new TransactionMaxSizeExceededException(details.getSize(), details.getMaxSize()));
                                return;
                            }
                        } catch (com.google.protobuf.InvalidProtocolBufferException ex) {
                            result.completeExceptionally(ex);
                            return;
                        }
                    }
                }
            }

            result.completeExceptionally(t);
        }

        @Override
        public void onCompleted() {
        }
    }
}
