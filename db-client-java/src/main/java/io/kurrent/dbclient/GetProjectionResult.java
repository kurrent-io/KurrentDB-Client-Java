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

class GetProjectionResult<TResult> {

    private final GrpcClient client;
    private final String projectionName;

    private final GetProjectionResultOptions options;

    private ThrowingBiFunction<JsonMapper, String, TResult, JsonProcessingException> deserializationStrategy;

    GetProjectionResult(final GrpcClient client, final String projectionName, GetProjectionResultOptions options, Class<TResult> resultType) {

        this(client, projectionName, options, (jsonMapper, json) -> jsonMapper.readValue(json, resultType));
    }

    GetProjectionResult(final GrpcClient client, final String projectionName, GetProjectionResultOptions options, Function<TypeFactory, JavaType> javaTypeFunction) {

        this(client, projectionName, options, (jsonMapper, json) -> jsonMapper.readValue(json, javaTypeFunction.apply(jsonMapper.getTypeFactory())));
    }

    public GetProjectionResult(final GrpcClient client, final String projectionName, GetProjectionResultOptions options, ThrowingBiFunction<JsonMapper, String, TResult, JsonProcessingException> deserializationStrategy) {

        this.client = client;
        this.projectionName = projectionName;
        this.options = options;
        this.deserializationStrategy = deserializationStrategy;
    }

    public CompletableFuture<TResult> execute() {

        return this.client.run(channel -> {

            Projectionmanagement.ResultReq.Options.Builder optionsBuilder =
                Projectionmanagement.ResultReq.Options.newBuilder()
                    .setName(projectionName);

            if(!options.getPartition().isEmpty()) {
                optionsBuilder.setPartition(options.getPartition());
            }

            Projectionmanagement.ResultReq request = Projectionmanagement.ResultReq.newBuilder()
                .setOptions(optionsBuilder)
                .build();

            ProjectionsGrpc.ProjectionsStub client = GrpcUtils.configureStub(ProjectionsGrpc.newStub(channel), this.client.getSettings(), this.options);

            CompletableFuture<TResult> result = new CompletableFuture<>();

            ThrowingFunction<Projectionmanagement.ResultResp, TResult, Exception> converter = source -> {

                String json = JsonFormat.printer().print(source.getResult());
                return deserializationStrategy.apply(new JsonMapper(), json);
            };

            client.result(request, GrpcUtils.convertSingleResponse(result, converter));

            return result;
        });
    }
}
