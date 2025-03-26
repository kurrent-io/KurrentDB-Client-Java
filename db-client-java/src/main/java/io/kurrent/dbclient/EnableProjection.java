package io.kurrent.dbclient;

import io.kurrent.dbclient.proto.projections.Projectionmanagement;
import io.kurrent.dbclient.proto.projections.ProjectionsGrpc;

import java.util.concurrent.CompletableFuture;

class EnableProjection {
    private final GrpcClient client;
    private final String projectionName;
    private EnableProjectionOptions options;

    public EnableProjection(final GrpcClient client, final String projectionName, final EnableProjectionOptions options) {
        this.client = client;
        this.projectionName = projectionName;
        this.options = options;
    }

    public CompletableFuture execute() {
        return this.client.run(channel -> {
            Projectionmanagement.EnableReq.Options.Builder optionsBuilder =
                    Projectionmanagement.EnableReq.Options.newBuilder()
                            .setName(this.projectionName);

            Projectionmanagement.EnableReq request = Projectionmanagement.EnableReq.newBuilder()
                    .setOptions(optionsBuilder)
                    .build();

            ProjectionsGrpc.ProjectionsStub client =
                    GrpcUtils.configureStub(ProjectionsGrpc.newStub(channel), this.client.getSettings(), this.options);

            CompletableFuture<Projectionmanagement.EnableResp> result = new CompletableFuture<>();

            client.enable(request, GrpcUtils.convertSingleResponse(result));

            return result;
        });
    }
}
