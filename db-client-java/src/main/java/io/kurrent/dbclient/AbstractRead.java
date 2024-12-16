package io.kurrent.dbclient;

import io.kurrent.dbclient.proto.shared.Shared;
import io.kurrent.dbclient.proto.streams.StreamsGrpc;
import io.kurrent.dbclient.proto.streams.StreamsOuterClass;
import io.grpc.Metadata;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.ClientCallStreamObserver;
import io.grpc.stub.ClientResponseObserver;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import java.util.concurrent.CompletableFuture;

abstract class AbstractRead implements Publisher<ReadMessage> {
    protected static final StreamsOuterClass.ReadReq.Options.Builder defaultReadOptions;

    private final GrpcClient client;
    private final OptionsBase<?> options;

    protected AbstractRead(GrpcClient client, OptionsBase<?> options) {
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
    @SuppressWarnings("unchecked")
    public void subscribe(Subscriber<? super ReadMessage> subscriber) {
        ReadSubscription readSubscription = new ReadSubscription(subscriber);
        subscriber.onSubscribe(readSubscription);

        CompletableFuture<ReadSubscription> result = new CompletableFuture<>();
        this.client.run(channel -> {
            StreamsOuterClass.ReadReq request = StreamsOuterClass.ReadReq.newBuilder()
                    .setOptions(createOptions())
                    .build();

            StreamsGrpc.StreamsStub client = GrpcUtils.configureStub(StreamsGrpc.newStub(channel), this.client.getSettings(), this.options);

            client.read(request, new ClientResponseObserver<StreamsOuterClass.ReadReq, StreamsOuterClass.ReadResp>() {
                @Override
                public void beforeStart(ClientCallStreamObserver<StreamsOuterClass.ReadReq> requestStream) {
                    readSubscription.setStreamObserver(requestStream);
                }

                private boolean completed = false;

                @Override
                public void onNext(StreamsOuterClass.ReadResp value) {
                    if (this.completed) {
                        return;
                    }
                    if (value.hasStreamNotFound()) {
                        StreamNotFoundException streamNotFoundException = new StreamNotFoundException();
                        handleError(streamNotFoundException);
                        return;
                    }

                    try {
                        readSubscription.onNext(new ReadMessage(value));
                    } catch (Throwable t) {
                        handleError(t);
                    }
                }

                @Override
                public void onCompleted() {
                    if (this.completed) {
                        return;
                    }
                    this.completed = true;
                    result.complete(readSubscription);
                    readSubscription.onCompleted();
                }

                @Override
                public void onError(Throwable t) {
                    if (this.completed) {
                        return;
                    }

                    if (t instanceof StatusRuntimeException) {
                        StatusRuntimeException e = (StatusRuntimeException) t;
                        String leaderHost = e.getTrailers().get(Metadata.Key.of("leader-endpoint-host", Metadata.ASCII_STRING_MARSHALLER));
                        String leaderPort = e.getTrailers().get(Metadata.Key.of("leader-endpoint-port", Metadata.ASCII_STRING_MARSHALLER));

                        if (leaderHost != null && leaderPort != null) {
                            NotLeaderException reason = new NotLeaderException(leaderHost, Integer.valueOf(leaderPort));
                            handleError(reason);
                            return;
                        }
                    }

                    handleError(t);
                }

                private void handleError(Throwable t) {
                    this.completed = true;
                    result.completeExceptionally(t);
                    readSubscription.onError(t);
                }
            });
            return result;
        }).exceptionally(t -> {
            readSubscription.onError(t);
            return readSubscription;
        });
    }
}
