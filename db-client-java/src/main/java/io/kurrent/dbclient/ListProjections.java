package io.kurrent.dbclient;

import io.kurrent.dbclient.proto.projections.Projectionmanagement;
import io.kurrent.dbclient.proto.projections.ProjectionsGrpc;
import io.kurrent.dbclient.proto.shared.Shared;
import io.grpc.stub.StreamObserver;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;


class ListProjections {
    private final GrpcClient client;
    private final ListProjectionsOptions options;

    public ListProjections(final GrpcClient client, final ListProjectionsOptions options) {
        this.client = client;
        this.options = options;
    }

    public CompletableFuture<ListProjectionsResult> execute() {
        return this.client.run(channel -> {
            Projectionmanagement.StatisticsReq.Options.Builder optionsBuilder =
                Projectionmanagement.StatisticsReq.Options.newBuilder()
                    .setContinuous(Shared.Empty.newBuilder());

            Projectionmanagement.StatisticsReq request = Projectionmanagement.StatisticsReq.newBuilder()
                .setOptions(optionsBuilder)
                .build();

            ProjectionsGrpc.ProjectionsStub client =
                GrpcUtils.configureStub(ProjectionsGrpc.newStub(channel), this.client.getSettings(), this.options);

            CompletableFuture<ListProjectionsResult> future = new CompletableFuture<>();
            ArrayList<ProjectionDetails> projections = new ArrayList<>();

            client.statistics(request, new StreamObserver<Projectionmanagement.StatisticsResp>() {
                @Override
                public void onNext(Projectionmanagement.StatisticsResp value) {
                    if (value.hasDetails()) {
                        projections.add(ProjectionDetails.fromWire(value.getDetails()));
                    }
                }

                @Override
                public void onCompleted() {
                    future.complete(new ListProjectionsResult(projections));
                }

                @Override
                public void onError(Throwable t) {
                    future.completeExceptionally(t);
                }
            });

            return future;
        });
    }
}
