package io.kurrent.dbclient;

import com.eventstore.dbclient.proto.projections.Projectionmanagement;
import com.eventstore.dbclient.proto.projections.ProjectionsGrpc;

import java.util.concurrent.CompletableFuture;

class ResetProjection {
    private final GrpcClient client;
    private final String projectionName;
    private final ResetProjectionOptions options;

    public ResetProjection(final GrpcClient client, final String projectionName, final ResetProjectionOptions options) {
        this.client = client;
        this.projectionName = projectionName;
        this.options = options;
    }

    public CompletableFuture execute() {
        return this.client.run(channel -> {
            Projectionmanagement.ResetReq.Options.Builder optionsBuilder =
                    Projectionmanagement.ResetReq.Options.newBuilder()
                            .setName(this.projectionName);

            Projectionmanagement.ResetReq request = Projectionmanagement.ResetReq.newBuilder()
                    .setOptions(optionsBuilder)
                    .build();

            ProjectionsGrpc.ProjectionsStub client =
                    GrpcUtils.configureStub(ProjectionsGrpc.newStub(channel), this.client.getSettings(), this.options);

            CompletableFuture<Projectionmanagement.ResetResp> result = new CompletableFuture<>();

            client.reset(request, GrpcUtils.convertSingleResponse(result));

            return result;
        });
    }
}
