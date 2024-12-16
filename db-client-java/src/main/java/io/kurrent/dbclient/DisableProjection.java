package io.kurrent.dbclient;

import com.eventstore.dbclient.proto.projections.Projectionmanagement;
import com.eventstore.dbclient.proto.projections.ProjectionsGrpc;

import java.util.concurrent.CompletableFuture;

class DisableProjection {
    private final GrpcClient client;
    private final String projectionName;
    private final DisableProjectionOptions options;

    public DisableProjection(final GrpcClient client, final String projectionName, final DisableProjectionOptions options) {
        this.client = client;
        this.projectionName = projectionName;
        this.options = options;
    }

    public CompletableFuture execute() {
        return this.client.run(channel -> {
            Projectionmanagement.DisableReq.Options.Builder optionsBuilder =
                    Projectionmanagement.DisableReq.Options.newBuilder()
                            .setName(this.projectionName)
                            .setWriteCheckpoint(true);

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
