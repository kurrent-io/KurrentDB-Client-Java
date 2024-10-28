package com.eventstore.dbclient;

import com.eventstore.dbclient.proto.persistentsubscriptions.Persistent;
import com.eventstore.dbclient.proto.persistentsubscriptions.PersistentSubscriptionsGrpc;
import com.eventstore.dbclient.proto.shared.Shared;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.ClientCallStreamObserver;
import io.grpc.stub.ClientResponseObserver;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.CompletableFuture;

abstract class AbstractSubscribePersistentSubscription {
    protected static final Persistent.ReadReq.Options.Builder defaultReadOptions;
    private final GrpcClient client;
    private final String group;
    private final PersistentSubscriptionListener listener;
    private final SubscribePersistentSubscriptionOptions options;

    static {
        defaultReadOptions = Persistent.ReadReq.Options.newBuilder()
                .setUuidOption(Persistent.ReadReq.Options.UUIDOption.newBuilder()
                        .setStructured(Shared.Empty.getDefaultInstance()));
    }

    public AbstractSubscribePersistentSubscription(GrpcClient client, String group,
                                                   SubscribePersistentSubscriptionOptions options,
                                                   PersistentSubscriptionListener listener) {
        this.client = client;
        this.group = group;
        this.options = options;
        this.listener = listener;
    }

    protected abstract Persistent.ReadReq.Options.Builder createOptions();

    public CompletableFuture<PersistentSubscription> execute() {
        return this.client.runWithArgs(args -> {
            PersistentSubscriptionsGrpc.PersistentSubscriptionsStub persistentSubscriptionsClient =
                    GrpcUtils.configureStub(PersistentSubscriptionsGrpc.newStub(args.getChannel()), this.client.getSettings(), this.options);

            final CompletableFuture<PersistentSubscription> result = new CompletableFuture<>();

            int bufferSize = this.options.getBufferSize();

            Persistent.ReadReq req = Persistent.ReadReq.newBuilder()
                    .setOptions(createOptions()
                            .setBufferSize(bufferSize)
                            .setGroupName(group))
                    .build();

            if (req.getOptions().hasAll() && !args.supportFeature(FeatureFlags.PERSISTENT_SUBSCRIPTION_TO_ALL)) {
                result.completeExceptionally(new UnsupportedFeatureException());
            } else {

                ClientResponseObserver<Persistent.ReadReq, Persistent.ReadResp> observer = new ClientResponseObserver<Persistent.ReadReq, Persistent.ReadResp>() {
                    private boolean _confirmed;
                    private PersistentSubscription _subscription;
                    private ClientCallStreamObserver<Persistent.ReadReq> _requestStream;

                    @Override
                    public void beforeStart(ClientCallStreamObserver<Persistent.ReadReq> requestStream) {
                        this._requestStream = requestStream;
                    }

                    @Override
                    public void onNext(Persistent.ReadResp readResp) {
                        if (!_confirmed && readResp.hasSubscriptionConfirmation()) {
                            this._confirmed = true;
                            this._subscription = new PersistentSubscription(this._requestStream,
                                    readResp.getSubscriptionConfirmation().getSubscriptionId());
                            result.complete(this._subscription);
                            listener.onConfirmation(this._subscription);
                            return;
                        }

                        if (!_confirmed && readResp.hasEvent()) {
                            onError(new IllegalStateException("Unconfirmed persistent subscription received event"));
                            return;
                        }

                        if (_confirmed && !readResp.hasEvent()) {
                            onError(new IllegalStateException(
                                    String.format("Confirmed persistent subscription %s received non-{event,checkpoint} variant",
                                            _subscription.getSubscriptionId())));
                            return;
                        }

                        int retryCount = readResp.getEvent().hasNoRetryCount() ? 0 : readResp.getEvent().getRetryCount();

                        try {
                            ResolvedEvent resolvedEvent = ResolvedEvent.fromWire(readResp.getEvent());
                            if (ClientTelemetry.isEnabled()) {
                                listener.onEvent(this._subscription, retryCount, resolvedEvent);
                            } else {
                                ClientTelemetry.traceSubscribe(
                                        () -> listener.onEvent(this._subscription, retryCount, resolvedEvent),
                                        _subscription.getSubscriptionId(),
                                        args.getChannel(),
                                        client.getSettings(),
                                        options.getCredentials(),
                                        resolvedEvent.getEvent());
                            }
                        } catch (Exception e) {
                            onError(e);
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        if (!_confirmed) {
                            result.completeExceptionally(throwable);
                        }

                        Throwable error = throwable;
                        if (error instanceof StatusRuntimeException) {
                            StatusRuntimeException sre = (StatusRuntimeException) error;
                            String desc = sre.getStatus().getDescription();
                            if (sre.getStatus().getCode() == Status.Code.CANCELLED && desc != null && desc.equals("user-initiated")) {
                                listener.onCancelled(this._subscription, null);
                                return;
                            }

                            String leaderHost = sre.getTrailers().get(Metadata.Key.of("leader-endpoint-host", Metadata.ASCII_STRING_MARSHALLER));
                            String leaderPort = sre.getTrailers().get(Metadata.Key.of("leader-endpoint-port", Metadata.ASCII_STRING_MARSHALLER));

                            if (leaderHost != null && leaderPort != null) {
                                error = new NotLeaderException(leaderHost, Integer.valueOf(leaderPort));
                            }
                        }

                        listener.onCancelled(this._subscription, error);
                    }

                    @Override
                    public void onCompleted() {
                        // Subscriptions should only complete on error.
                    }
                };

                StreamObserver<Persistent.ReadReq> wireStream = persistentSubscriptionsClient.read(observer);
                wireStream.onNext(req);
            }

            return result;
        });
    }
}
