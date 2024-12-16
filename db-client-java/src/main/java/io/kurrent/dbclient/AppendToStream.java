package io.kurrent.dbclient;

import io.kurrent.dbclient.proto.shared.Shared;
import io.kurrent.dbclient.proto.streams.StreamsGrpc;
import io.kurrent.dbclient.proto.streams.StreamsOuterClass;
import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

class AppendToStream {
    private final GrpcClient client;
    private final String streamName;
    private final List<EventData> events;
    private final AppendToStreamOptions options;

    public AppendToStream(GrpcClient client, String streamName, Iterator<EventData> events, AppendToStreamOptions options) {
        this.client = client;
        this.streamName = streamName;
        this.events = new ArrayList<>();
        while (events.hasNext()) {
            this.events.add(events.next());
        }
        this.options = options;
    }

    public CompletableFuture<WriteResult> execute() {
        return this.client.run(channel -> ClientTelemetry.traceAppend(
                this::append,
                channel,
                events,
                this.streamName,
                this.client.getSettings(),
                this.options.getCredentials()));
    }

    private CompletableFuture<WriteResult> append(ManagedChannel channel, List<EventData> events) {
        CompletableFuture<WriteResult> result = new CompletableFuture<>();
        StreamsOuterClass.AppendReq.Options.Builder options = this.options.getExpectedRevision().applyOnWire(StreamsOuterClass.AppendReq.Options.newBuilder()
                .setStreamIdentifier(Shared.StreamIdentifier.newBuilder()
                        .setStreamName(ByteString.copyFromUtf8(streamName))
                        .build()));
        StreamsGrpc.StreamsStub client = GrpcUtils.configureStub(StreamsGrpc.newStub(channel), this.client.getSettings(), this.options);

        StreamObserver<StreamsOuterClass.AppendReq> requestStream = client.append(GrpcUtils.convertSingleResponse(result, resp -> {
            if (resp.hasSuccess()) {
                StreamsOuterClass.AppendResp.Success success = resp.getSuccess();

                Position logPosition = null;
                if (success.getPositionOptionCase() == StreamsOuterClass.AppendResp.Success.PositionOptionCase.POSITION) {
                    StreamsOuterClass.AppendResp.Position p = success.getPosition();
                    logPosition = new Position(p.getCommitPosition(), p.getPreparePosition());
                }

                ExpectedRevision nextExpectedRevision = success.hasNoStream() ? ExpectedRevision.noStream()
                        : ExpectedRevision.expectedRevision(success.getCurrentRevision());

                return new WriteResult(nextExpectedRevision, logPosition);
            }
            if (resp.hasWrongExpectedVersion()) {
                StreamsOuterClass.AppendResp.WrongExpectedVersion wev = resp.getWrongExpectedVersion();

                ExpectedRevision expectedRevision;
                if (wev.getExpectedRevisionOptionCase() == StreamsOuterClass.AppendResp.WrongExpectedVersion.ExpectedRevisionOptionCase.EXPECTED_ANY) {
                    expectedRevision = ExpectedRevision.any();
                } else if (wev.getExpectedRevisionOptionCase() == StreamsOuterClass.AppendResp.WrongExpectedVersion.ExpectedRevisionOptionCase.EXPECTED_STREAM_EXISTS) {
                    expectedRevision = ExpectedRevision.streamExists();
                } else {
                    expectedRevision = ExpectedRevision.expectedRevision(wev.getExpectedRevision());
                }

                ExpectedRevision currentRevision;
                if (wev.getCurrentRevisionOptionCase() == StreamsOuterClass.AppendResp.WrongExpectedVersion.CurrentRevisionOptionCase.CURRENT_NO_STREAM) {
                    currentRevision = ExpectedRevision.noStream();
                } else {
                    currentRevision = ExpectedRevision.expectedRevision(wev.getCurrentRevision());
                }

                String streamName = options.getStreamIdentifier().getStreamName().toStringUtf8();

                throw new WrongExpectedVersionException(streamName, expectedRevision, currentRevision);
            }

            throw new IllegalStateException("AppendResponse has neither Success or WrongExpectedVersion variants");
        }));

        try {
            requestStream.onNext(StreamsOuterClass.AppendReq.newBuilder().setOptions(options).build());

            for (EventData e : events) {
                StreamsOuterClass.AppendReq.ProposedMessage.Builder msgBuilder = StreamsOuterClass.AppendReq.ProposedMessage.newBuilder()
                        .setId(Shared.UUID.newBuilder()
                                .setStructured(Shared.UUID.Structured.newBuilder()
                                        .setMostSignificantBits(e.getEventId().getMostSignificantBits())
                                        .setLeastSignificantBits(e.getEventId().getLeastSignificantBits())))
                        .setData(ByteString.copyFrom(e.getEventData()))
                        .putMetadata(SystemMetadataKeys.CONTENT_TYPE, e.getContentType())
                        .putMetadata(SystemMetadataKeys.TYPE, e.getEventType());

                if (e.getUserMetadata() != null) {
                    msgBuilder.setCustomMetadata(ByteString.copyFrom(e.getUserMetadata()));
                }

                requestStream.onNext(StreamsOuterClass.AppendReq.newBuilder()
                        .setProposedMessage(msgBuilder)
                        .build());
            }
            requestStream.onCompleted();
        } catch (StatusRuntimeException e) {
            String leaderHost = e.getTrailers().get(Metadata.Key.of("leader-endpoint-host", Metadata.ASCII_STRING_MARSHALLER));
            String leaderPort = e.getTrailers().get(Metadata.Key.of("leader-endpoint-port", Metadata.ASCII_STRING_MARSHALLER));

            if (leaderHost != null && leaderPort != null) {
                NotLeaderException reason = new NotLeaderException(leaderHost, Integer.valueOf(leaderPort));
                result.completeExceptionally(reason);
            } else {
                result.completeExceptionally(e);
            }
        } catch (RuntimeException e) {
            result.completeExceptionally(e);
        }

        return result;
    }
}
