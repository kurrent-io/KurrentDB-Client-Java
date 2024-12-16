package io.kurrent.dbclient;

import com.eventstore.dbclient.proto.projections.Projectionmanagement;
import com.eventstore.dbclient.proto.projections.ProjectionsGrpc;
import com.eventstore.dbclient.proto.shared.Shared;

import java.util.concurrent.CompletableFuture;

class UpdateProjection {
    private final GrpcClient client;
    private final String projectionName;
    private final String query;
    private final Boolean emitEnabled;
    private final UpdateProjectionOptions options;

    public UpdateProjection(final GrpcClient client, final String projectionName, final String query,
                            final UpdateProjectionOptions options) {
        this.client = client;
        this.projectionName = projectionName;
        this.query = query;
        this.emitEnabled = options.isEmitEnabled();
        this.options = options;
    }

    public CompletableFuture execute() {
        return this.client.run(channel -> {
            Projectionmanagement.UpdateReq.Options.Builder optionsBuilder =
                Projectionmanagement.UpdateReq.Options.newBuilder()
                    .setName(this.projectionName)
                    .setQuery(this.query);

            if(this.emitEnabled == null){
                optionsBuilder.setNoEmitOptions(Shared.Empty.newBuilder());
            } else {
                optionsBuilder.setEmitEnabled(this.emitEnabled);
            }

            Projectionmanagement.UpdateReq request = Projectionmanagement.UpdateReq.newBuilder()
                .setOptions(optionsBuilder)
                .build();

            ProjectionsGrpc.ProjectionsStub client =
                    GrpcUtils.configureStub(ProjectionsGrpc.newStub(channel), this.client.getSettings(), this.options);

            CompletableFuture<Projectionmanagement.UpdateResp> result = new CompletableFuture<>();

            client.update(request, GrpcUtils.convertSingleResponse(result));

            return result;
        });
    }
}
