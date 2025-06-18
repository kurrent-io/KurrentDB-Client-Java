package io.kurrent.dbclient;

import io.kurrent.dbclient.proto.shared.Shared;
import io.kurrent.dbclient.proto.streams.StreamsGrpc;
import io.kurrent.dbclient.proto.streams.StreamsOuterClass;
import io.grpc.ManagedChannel;

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
        return this.client.run(channel -> {
            CompletableFuture<Subscription> future = new CompletableFuture<>();

            StreamsOuterClass.ReadReq readReq = StreamsOuterClass.ReadReq.newBuilder()
                    .setOptions(createOptions())
                    .build();

            StreamsGrpc.StreamsStub streamsClient = GrpcUtils.configureStub(
                    StreamsGrpc.newStub(channel),
                    this.client.getSettings(),
                    this.options
            );

            ReadResponseObserver observer = createObserver(channel, future);
            streamsClient.read(readReq, observer);

            return future;
        });
    }

    private ReadResponseObserver createObserver(ManagedChannel channel, CompletableFuture<Subscription> future) {
        StreamConsumer consumer = new SubscriptionStreamConsumer(
                this.listener,
                this.checkpointer,
                future,
                (subscriptionId, event, action) -> {
                    ClientTelemetry.traceSubscribe(
                            action,
                            subscriptionId,
                            channel,
                            client.getSettings(),
                            options.getCredentials(),
                            event
                    );
                }
        );

        return new ReadResponseObserver(
                this.options, 
                consumer, 
                this.client.getSerializer(options.serializationSettings().orElse(null))
        );
    }
}
