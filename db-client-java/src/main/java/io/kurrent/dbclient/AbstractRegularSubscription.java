package io.kurrent.dbclient;

import io.kurrent.dbclient.proto.shared.Shared;
import io.kurrent.dbclient.proto.streams.StreamsGrpc;
import io.kurrent.dbclient.proto.streams.StreamsOuterClass;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.ClientCallStreamObserver;
import io.grpc.stub.ClientResponseObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

abstract class AbstractRegularSubscription {
    protected static final StreamsOuterClass.ReadReq.Options.Builder defaultReadOptions;
    protected static final StreamsOuterClass.ReadReq.Options.Builder defaultSubscribeOptions;

    protected SubscriptionListener listener;
    protected Checkpointer checkpointer = null;
    private final GrpcClient client;
    private final OptionsWithBackPressureAndSerialization<?> options;

    protected AbstractRegularSubscription(GrpcClient client, OptionsWithBackPressureAndSerialization<?> options) {
        this.client = client;
        this.options = options;
    }

    static {
        defaultReadOptions = StreamsOuterClass.ReadReq.Options.newBuilder()
                .setUuidOption(StreamsOuterClass.ReadReq.Options.UUIDOption.newBuilder()
                        .setStructured(Shared.Empty.getDefaultInstance()));
        defaultSubscribeOptions = defaultReadOptions.clone()
                .setReadDirection(StreamsOuterClass.ReadReq.Options.ReadDirection.Forwards)
                .setSubscription(StreamsOuterClass.ReadReq.Options.SubscriptionOptions.getDefaultInstance());
    }

    protected abstract StreamsOuterClass.ReadReq.Options.Builder createOptions();

    public CompletableFuture<Subscription> execute() {
        CompletableFuture<Subscription> future = new CompletableFuture<>();

        this.client.getWorkItemArgs().whenComplete((args, error) -> {
            if (error != null) {
                future.completeExceptionally(error);
                return;
            }

            ReadResponseObserver observer = createObserver(args, future);
            observer.onConnected(args);

            StreamsOuterClass.ReadReq readReq = StreamsOuterClass.ReadReq.newBuilder()
                    .setOptions(createOptions())
                    .build();

            StreamsGrpc.StreamsStub streamsClient = GrpcUtils.configureStub(StreamsGrpc.newStub(args.getChannel()), this.client.getSettings(), this.options);
            streamsClient.read(readReq, observer);
        });

        return future;
    }

    private ReadResponseObserver createObserver(WorkItemArgs args, CompletableFuture<Subscription> future) {
        StreamConsumer consumer = new SubscriptionStreamConsumer(this.listener, this.checkpointer, future, (subscriptionId, event, action) -> {
            ClientTelemetry.traceSubscribe(
                    action,
                    subscriptionId,
                    args.getChannel(),
                    client.getSettings(),
                    options.getCredentials(),
                    event);
        });

        return new ReadResponseObserver(
                this.options, 
                consumer, 
                this.client.getSerializer(options.serializationSettings().orElse(null))
        );
    }
}
