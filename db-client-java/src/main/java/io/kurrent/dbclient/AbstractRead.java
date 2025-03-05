package io.kurrent.dbclient;

import io.kurrent.dbclient.proto.shared.Shared;
import io.kurrent.dbclient.proto.streams.StreamsGrpc;
import io.kurrent.dbclient.proto.streams.StreamsOuterClass;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

abstract class AbstractRead implements Publisher<ReadMessage> {
    protected static final StreamsOuterClass.ReadReq.Options.Builder defaultReadOptions;

    private final GrpcClient client;
    private final OptionsWithBackPressure<?> options;

    protected AbstractRead(GrpcClient client, OptionsWithBackPressure<?> options) {
        this.client = client;
        this.options = options;
    }

    static {
        defaultReadOptions = StreamsOuterClass.ReadReq.Options.newBuilder()
                .setUuidOption(StreamsOuterClass.ReadReq.Options.UUIDOption.newBuilder()
                        .setStructured(Shared.Empty.getDefaultInstance()));
    }

    public abstract StreamsOuterClass.ReadReq.Options.Builder createOptions();

    @Override
    public void subscribe(Subscriber<? super ReadMessage> subscriber) {
        ReadResponseObserver observer = new ReadResponseObserver(options, new ReadStreamConsumer(subscriber));

        this.client.getWorkItemArgs().whenComplete((args, error) -> {
           if (error != null) {
               observer.onError(error);
               return;
           }

            StreamsOuterClass.ReadReq request = StreamsOuterClass.ReadReq.newBuilder()
                    .setOptions(createOptions())
                    .build();

            StreamsGrpc.StreamsStub client = GrpcUtils.configureStub(StreamsGrpc.newStub(args.getChannel()), this.client.getSettings(), this.options);
            observer.onConnected(args);
            subscriber.onSubscribe(observer.getSubscription());
            client.read(request, observer);
        });
    }
}
