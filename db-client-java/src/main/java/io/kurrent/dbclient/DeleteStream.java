package io.kurrent.dbclient;

import io.kurrent.dbclient.proto.shared.Shared;
import io.kurrent.dbclient.proto.streams.StreamsGrpc;
import io.kurrent.dbclient.proto.streams.StreamsOuterClass;
import com.google.protobuf.ByteString;

import java.util.concurrent.CompletableFuture;

class DeleteStream {
    private final GrpcClient client;
    private final String streamName;
    private final boolean softDelete;

    private final DeleteStreamOptions options;

    public DeleteStream(GrpcClient client, String streamName, boolean softDelete, DeleteStreamOptions options) {
        this.client = client;
        this.streamName = streamName;
        this.softDelete = softDelete;
        this.options = options;
    }

    public CompletableFuture<DeleteResult> execute() {
        return this.client.run(channel -> {
            StreamsGrpc.StreamsStub client = GrpcUtils.configureStub(StreamsGrpc.newStub(channel), this.client.getSettings(), this.options);

            if (this.softDelete) {
                StreamsOuterClass.DeleteReq req = StreamsOuterClass.DeleteReq.newBuilder()
                        .setOptions(this.options.getStreamState().applyOnWire(StreamsOuterClass.DeleteReq.Options.newBuilder()
                                .setStreamIdentifier(Shared.StreamIdentifier.newBuilder()
                                        .setStreamName(ByteString.copyFromUtf8(streamName))
                                        .build())))
                        .build();

                CompletableFuture<DeleteResult> result = new CompletableFuture<>();
                client.delete(req, GrpcUtils.convertSingleResponse(result, resp -> {
                    final long commitUnsigned = resp.getPosition().getCommitPosition();
                    final long prepareUnsigned = resp.getPosition().getPreparePosition();

                    return new DeleteResult(new Position(commitUnsigned, prepareUnsigned));
                }));
                return result;
            }

            StreamsOuterClass.TombstoneReq req = StreamsOuterClass.TombstoneReq.newBuilder()
                    .setOptions(this.options.getStreamState().applyOnWire(StreamsOuterClass.TombstoneReq.Options.newBuilder()
                            .setStreamIdentifier(Shared.StreamIdentifier.newBuilder()
                                    .setStreamName(ByteString.copyFromUtf8(streamName))
                                    .build())))
                    .build();

            CompletableFuture<DeleteResult> result = new CompletableFuture<>();
            client.tombstone(req, GrpcUtils.convertSingleResponse(result, resp -> {
                final long commitUnsigned = resp.getPosition().getCommitPosition();
                final long prepareUnsigned = resp.getPosition().getPreparePosition();

                return new DeleteResult(new Position(commitUnsigned, prepareUnsigned));
            }));
            return result;
        });
    }
}
