package io.kurrent.dbclient;

import com.eventstore.dbclient.proto.projections.Projectionmanagement;
import com.eventstore.dbclient.proto.projections.ProjectionsGrpc;

import java.util.concurrent.CompletableFuture;


class GetProjectionStatus {
    private final GrpcClient client;
    private final String projectionName;
    private final GetProjectionStatusOptions options;

    public GetProjectionStatus(final GrpcClient client, final String projectionName, final GetProjectionStatusOptions options) {
        this.client = client;
        this.projectionName = projectionName;
        this.options = options;
    }

    public CompletableFuture<ProjectionDetails> execute() {
        return this.client.run(channel -> {
            Projectionmanagement.StatisticsReq.Options.Builder optionsBuilder =
                    Projectionmanagement.StatisticsReq.Options.newBuilder()
                            .setName(this.projectionName);

            Projectionmanagement.StatisticsReq request = Projectionmanagement.StatisticsReq.newBuilder()
                    .setOptions(optionsBuilder)
                    .build();

            ProjectionsGrpc.ProjectionsStub client =
                    GrpcUtils.configureStub(ProjectionsGrpc.newStub(channel), this.client.getSettings(), this.options);

            CompletableFuture<ProjectionDetails> result = new CompletableFuture<>();

            client.statistics(request, GrpcUtils.convertSingleResponse(result, resp -> {
                final Projectionmanagement.StatisticsResp.Details details = resp.getDetails();
                return ProjectionDetails.fromWire(details);
            }));

            return result;
        });
    }
}
