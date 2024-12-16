package io.kurrent.dbclient;

import io.kurrent.dbclient.proto.projections.ProjectionsGrpc;
import io.kurrent.dbclient.proto.shared.Shared;

import java.util.concurrent.CompletableFuture;

class RestartProjectionSubsystem {
    private final GrpcClient client;
    private final RestartProjectionSubsystemOptions options;

    public RestartProjectionSubsystem(final GrpcClient client, final RestartProjectionSubsystemOptions options) {
        this.client = client;
        this.options = options;
    }

    public CompletableFuture execute() {
        return this.client.run(channel -> {
            ProjectionsGrpc.ProjectionsStub client =
                    GrpcUtils.configureStub(ProjectionsGrpc.newStub(channel), this.client.getSettings(), this.options);

            CompletableFuture<Shared.Empty> result = new CompletableFuture<>();

            Shared.Empty request = Shared.Empty.newBuilder().build();

            client.restartSubsystem(request, GrpcUtils.convertSingleResponse(result));

            return result;
        });
    }
}
