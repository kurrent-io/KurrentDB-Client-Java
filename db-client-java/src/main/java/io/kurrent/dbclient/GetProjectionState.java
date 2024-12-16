package io.kurrent.dbclient;

import com.eventstore.dbclient.proto.projections.Projectionmanagement;
import com.eventstore.dbclient.proto.projections.ProjectionsGrpc;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.protobuf.util.JsonFormat;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

class GetProjectionState<TResult> {

    private final GrpcClient client;
    private final String projectionName;
    private final GetProjectionStateOptions options;

    private ThrowingBiFunction<JsonMapper, String, TResult, JsonProcessingException> deserializationStrategy;

    GetProjectionState(final GrpcClient client,
                       final String projectionName,
                       GetProjectionStateOptions options,
                       Class<TResult> resultType) {

        this(client, projectionName, options, (jsonMapper, json) -> jsonMapper.readValue(json, resultType));
    }

    GetProjectionState(final GrpcClient client,
                       final String projectionName,
                       GetProjectionStateOptions options,
                       Function<TypeFactory, JavaType> javaTypeFunction) {

        this(client, projectionName, options, (jsonMapper, json) -> jsonMapper.readValue(json, javaTypeFunction.apply(jsonMapper.getTypeFactory())));
    }

    public GetProjectionState(final GrpcClient client,
                              final String projectionName,
                              GetProjectionStateOptions options,
                              ThrowingBiFunction<JsonMapper, String, TResult, JsonProcessingException> deserializationStrategy) {

        this.client = client;
        this.projectionName = projectionName;
        this.options = options;
        this.deserializationStrategy = deserializationStrategy;
    }

    public CompletableFuture<TResult> execute() {

        return this.client.run(channel -> {

            Projectionmanagement.StateReq.Options.Builder optionsBuilder =
                    Projectionmanagement.StateReq.Options.newBuilder()
                            .setName(projectionName);

            if(!options.getPartition().isEmpty()) {
                optionsBuilder.setPartition(options.getPartition());
            }

            Projectionmanagement.StateReq request = Projectionmanagement.StateReq.newBuilder()
                    .setOptions(optionsBuilder)
                    .build();

            ProjectionsGrpc.ProjectionsStub client = GrpcUtils.configureStub(ProjectionsGrpc.newStub(channel), this.client.getSettings(), this.options);

            CompletableFuture<TResult> result = new CompletableFuture<>();

            ThrowingFunction<Projectionmanagement.StateResp, TResult, Exception> converter = source -> {

                String json = JsonFormat.printer().print(source.getState());
                return deserializationStrategy.apply(new JsonMapper(), json);
            };

            client.state(request, GrpcUtils.convertSingleResponse(result, converter));

            return result;
        });
    }
}
