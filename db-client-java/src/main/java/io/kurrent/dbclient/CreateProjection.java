package io.kurrent.dbclient;

import com.eventstore.dbclient.proto.projections.Projectionmanagement;
import com.eventstore.dbclient.proto.projections.ProjectionsGrpc;

import java.util.concurrent.CompletableFuture;

class CreateProjection {

    private final GrpcClient client;
    private final String projectionName;
    private final String query;
    private final boolean trackEmittedStreams;
    private final boolean emitEnabled;
    private final CreateProjectionOptions options;

    public CreateProjection(final GrpcClient client, final String projectionName, final String query,
                            final CreateProjectionOptions options) {

        this.client = client;
        this.projectionName = projectionName;
        this.query = query;
        this.trackEmittedStreams = options.isTrackingEmittedStreams();
        this.emitEnabled = options.isEmitEnabled();
        this.options = options;
    }

    @SuppressWarnings("unchecked")
    public CompletableFuture execute() {
        return this.client.run(channel -> {
            Projectionmanagement.CreateReq.Options.Continuous.Builder continuousBuilder =
                    Projectionmanagement.CreateReq.Options.Continuous.newBuilder()
                            .setName(projectionName)
                            .setTrackEmittedStreams(trackEmittedStreams);

            Projectionmanagement.CreateReq.Options.Builder optionsBuilder =
                Projectionmanagement.CreateReq.Options.newBuilder()
                    .setQuery(query)
                    .setContinuous(continuousBuilder);

            Projectionmanagement.CreateReq request = Projectionmanagement.CreateReq.newBuilder()
                    .setOptions(optionsBuilder)
                    .build();

            ProjectionsGrpc.ProjectionsStub client = GrpcUtils.configureStub(ProjectionsGrpc.newStub(channel), this.client.getSettings(), this.options);

            CompletableFuture<Projectionmanagement.CreateResp> result = new CompletableFuture<>();

            client.create(request, GrpcUtils.convertSingleResponse(result));

            return result;
        }).thenApplyAsync(result -> {
            if (emitEnabled) {
                UpdateProjectionOptions options = UpdateProjectionOptions.get().emitEnabled(true);
                UpdateProjection update = new UpdateProjection(client, projectionName, query, options);

                return update.execute().thenApply(x -> result);
            }

            return CompletableFuture.completedFuture(result);
        });
    }
}
