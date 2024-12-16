package io.kurrent.dbclient;

import com.eventstore.dbclient.proto.projections.Projectionmanagement;
import com.eventstore.dbclient.proto.projections.ProjectionsGrpc;

import java.util.concurrent.CompletableFuture;

class DeleteProjection {
    private final GrpcClient client;
    private final String projectionName;
    private final DeleteProjectionOptions options;

    public DeleteProjection(final GrpcClient client, final String projectionName, final DeleteProjectionOptions options) {
        this.client = client;
        this.projectionName = projectionName;
        this.options = options;
    }

    public CompletableFuture execute() {
        return this.client.run(channel -> {
            Projectionmanagement.DeleteReq.Options reqOptions =
                    Projectionmanagement.DeleteReq.Options.newBuilder()
                            .setName(this.projectionName)
                            .setDeleteCheckpointStream(options.getDeleteCheckpointStream())
                            .setDeleteEmittedStreams(options.getDeleteEmittedStreams())
                            .setDeleteStateStream(options.getDeleteStateStream())
                            .build();

            Projectionmanagement.DeleteReq request = Projectionmanagement.DeleteReq.newBuilder()
                    .setOptions(reqOptions)
                    .build();

            ProjectionsGrpc.ProjectionsStub client =
                    GrpcUtils.configureStub(ProjectionsGrpc.newStub(channel), this.client.getSettings(), this.options);

            CompletableFuture<Projectionmanagement.DeleteResp> result = new CompletableFuture<>();

            client.delete(request, GrpcUtils.convertSingleResponse(result));

            return result;
        });
    }
}
