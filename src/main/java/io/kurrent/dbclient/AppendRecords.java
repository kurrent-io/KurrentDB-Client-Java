package io.kurrent.dbclient;

import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import com.google.protobuf.Value;
import io.grpc.StatusRuntimeException;
import io.grpc.protobuf.StatusProto;
import io.grpc.stub.ClientCallStreamObserver;
import io.grpc.stub.ClientResponseObserver;
import io.kurrentdb.protocol.v2.streams.AppendRecordsRequest;
import io.kurrentdb.protocol.v2.streams.SchemaInfo;
import io.kurrentdb.protocol.v2.streams.StreamsServiceGrpc;

import io.kurrentdb.protocol.v2.streams.errors.AppendConsistencyViolationErrorDetails;
import io.kurrentdb.protocol.v2.streams.errors.AppendRecordSizeExceededErrorDetails;
import io.kurrentdb.protocol.v2.streams.errors.AppendTransactionSizeExceededErrorDetails;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

class AppendRecords {
    private final GrpcClient client;
    private final List<AppendRecord> records;
    private final List<ConsistencyCheck> checks;

    public AppendRecords(GrpcClient client, List<AppendRecord> records, List<ConsistencyCheck> checks) {
        this.client = client;
        this.records = records;
        this.checks = checks;
    }

    public CompletableFuture<AppendRecordsResponse> execute() {
        if (records.isEmpty()) {
            CompletableFuture<AppendRecordsResponse> result = new CompletableFuture<>();
            result.completeExceptionally(new IllegalArgumentException("At least one record is required."));
            return result;
        }

        return this.client.runWithArgs(args -> ClientTelemetry.traceAppendRecords(
                this::appendRecords,
                args,
                this.records,
                this.client.getSettings()));
    }

    private CompletableFuture<AppendRecordsResponse> appendRecords(WorkItemArgs args, List<AppendRecord> records) {
        CompletableFuture<AppendRecordsResponse> result = new CompletableFuture<>();

        if (!args.supportFeature(FeatureFlags.APPEND_RECORDS)) {
            result.completeExceptionally(new UnsupportedOperationException(
                    "AppendRecords is not supported by the server. Requires KurrentDB 26.1 or later."));
            return result;
        }

        StreamsServiceGrpc.StreamsServiceStub stub = GrpcUtils.configureStub(
                StreamsServiceGrpc.newStub(args.getChannel()),
                this.client.getSettings(),
                new OptionsBase<>(),
                null,
                false);

        try {
            AppendRecordsRequest.Builder requestBuilder = AppendRecordsRequest.newBuilder();

            for (AppendRecord record : records) {
                EventData event = record.getRecord();

                io.kurrentdb.protocol.v2.streams.AppendRecord.Builder recordBuilder =
                        io.kurrentdb.protocol.v2.streams.AppendRecord.newBuilder()
                                .setStream(record.getStream())
                                .setData(ByteString.copyFrom(event.getEventData()))
                                .setRecordId(event.getEventId().toString())
                                .setSchema(SchemaInfo.newBuilder()
                                        .setFormat(ContentTypeMapper.toSchemaDataFormat(event.getContentType()))
                                        .setName(event.getEventType()));

                if (event.getUserMetadata() != null) {
                    Map<String, Value> properties = DynamicValueMapper.mapJsonToValueMap(event.getUserMetadata());
                    recordBuilder.putAllProperties(properties);
                }

                requestBuilder.addRecords(recordBuilder.build());
            }

            if (checks != null) {
                for (ConsistencyCheck check : checks) {
                    if (check instanceof ConsistencyCheck.StreamStateCheck) {
                        ConsistencyCheck.StreamStateCheck stateCheck = (ConsistencyCheck.StreamStateCheck) check;
                        requestBuilder.addChecks(
                                io.kurrentdb.protocol.v2.streams.ConsistencyCheck.newBuilder()
                                        .setStreamState(
                                                io.kurrentdb.protocol.v2.streams.ConsistencyCheck.StreamStateCheck.newBuilder()
                                                        .setStream(stateCheck.getStream())
                                                        .setExpectedState(stateCheck.getExpectedState().toRawLong())
                                                        .build())
                                        .build());
                    }
                }
            }

            stub.appendRecords(requestBuilder.build(), new AppendRecordsObserver(result));
        } catch (RuntimeException e) {
            result.completeExceptionally(e);
        }

        return result;
    }

    private AppendRecordsResponse onResponse(io.kurrentdb.protocol.v2.streams.AppendRecordsResponse response) {
        List<AppendResponse> results = new ArrayList<>(response.getRevisionsCount());

        for (io.kurrentdb.protocol.v2.streams.StreamRevision revision : response.getRevisionsList()) {
            results.add(new AppendResponse(revision.getStream(), revision.getRevision()));
        }

        return new AppendRecordsResponse(response.getPosition(), results);
    }

    private class AppendRecordsObserver implements ClientResponseObserver<AppendRecordsRequest, io.kurrentdb.protocol.v2.streams.AppendRecordsResponse> {
        private final CompletableFuture<AppendRecordsResponse> result;

        public AppendRecordsObserver(CompletableFuture<AppendRecordsResponse> result) {
            this.result = result;
        }

        @Override
        public void beforeStart(ClientCallStreamObserver<AppendRecordsRequest> requestStream) {
        }

        @Override
        public void onNext(io.kurrentdb.protocol.v2.streams.AppendRecordsResponse response) {
            try {
                AppendRecordsResponse converted = onResponse(response);
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
                            if (d.is(AppendConsistencyViolationErrorDetails.class)) {
                                AppendConsistencyViolationErrorDetails details =
                                        d.unpack(AppendConsistencyViolationErrorDetails.class);
                                List<ConsistencyViolation> violations = new ArrayList<>();

                                for (io.kurrentdb.protocol.v2.streams.errors.ConsistencyViolation v : details.getViolationsList()) {
                                    if (v.hasStreamState()) {
                                        io.kurrentdb.protocol.v2.streams.errors.ConsistencyViolation.StreamStateViolation ss =
                                                v.getStreamState();
                                        violations.add(new ConsistencyViolation(
                                                v.getCheckIndex(),
                                                ss.getStream(),
                                                StreamState.fromRawLong(ss.getExpectedState()),
                                                StreamState.fromRawLong(ss.getActualState())));
                                    }
                                }

                                result.completeExceptionally(new AppendConsistencyViolationException(violations));
                                return;
                            } else if (d.is(AppendRecordSizeExceededErrorDetails.class)) {
                                AppendRecordSizeExceededErrorDetails details =
                                        d.unpack(AppendRecordSizeExceededErrorDetails.class);
                                result.completeExceptionally(new RecordSizeExceededException(
                                        details.getStream(), details.getRecordId(),
                                        details.getSize(), details.getMaxSize()));
                                return;
                            } else if (d.is(AppendTransactionSizeExceededErrorDetails.class)) {
                                AppendTransactionSizeExceededErrorDetails details =
                                        d.unpack(AppendTransactionSizeExceededErrorDetails.class);
                                result.completeExceptionally(new TransactionMaxSizeExceededException(
                                        details.getSize(), details.getMaxSize()));
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
