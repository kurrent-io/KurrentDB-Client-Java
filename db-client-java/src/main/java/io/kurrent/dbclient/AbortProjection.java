package io.kurrent.dbclient;

import io.kurrent.dbclient.proto.projections.Projectionmanagement;
import io.kurrent.dbclient.proto.projections.ProjectionsGrpc;

import java.util.concurrent.CompletableFuture;

class AbortProjection {
    private final GrpcClient client;
    private final String projectionName;
    private final AbortProjectionOptions options;

    public AbortProjection(final GrpcClient client, final String projectionName, final AbortProjectionOptions options) {
        this.client = client;
        this.projectionName = projectionName;
        this.options = options;
    }

    public CompletableFuture execute() {
        return this.client.run(channel -> {
            Projectionmanagement.DisableReq.Options.Builder optionsBuilder =
                    Projectionmanagement.DisableReq.Options.newBuilder()
                            .setName(this.projectionName)
                            .setWriteCheckpoint(false);

            Projectionmanagement.DisableReq request = Projectionmanagement.DisableReq.newBuilder()
                    .setOptions(optionsBuilder)
                    .build();

            ProjectionsGrpc.ProjectionsStub client =
                    GrpcUtils.configureStub(ProjectionsGrpc.newStub(channel), this.client.getSettings(), this.options);

            CompletableFuture<Projectionmanagement.DisableResp> result = new CompletableFuture<>();

            client.disable(request, GrpcUtils.convertSingleResponse(result));

            return result;
        });
    }
}
