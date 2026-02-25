package io.kurrent.dbclient;


import java.time.Instant;
import java.util.concurrent.CompletableFuture;

class SubscriptionStreamConsumer implements StreamConsumer {
    private final SubscriptionListener listener;
    private final Checkpointer checkpointer;
    private final CompletableFuture<Subscription> future;
    private final SubscriptionTracingCallback tracing;
    private org.reactivestreams.Subscription internal;
    private Subscription subscription;

    public SubscriptionStreamConsumer(SubscriptionListener listener, Checkpointer checkpointer, CompletableFuture<Subscription> future, SubscriptionTracingCallback tracing) {
        this.listener = listener;
        this.checkpointer = checkpointer;
        this.future = future;
        this.tracing = tracing;
    }

    @Override
    public void onSubscribe(org.reactivestreams.Subscription subscription) {
        this.internal = subscription;
    }

    @Override
    public void onEvent(ResolvedEvent event) {
        this.tracing.trace(this.subscription.getSubscriptionId(), event.getEvent(), () -> this.listener.onEvent(this.subscription, event));
    }

    @Override
    public void onSubscriptionConfirmation(String subscriptionId) {
        this.subscription = new Subscription(this.internal, subscriptionId, this.checkpointer);
        this.future.complete(this.subscription);
        this.listener.onConfirmation(this.subscription);
    }

    @Override
    public void onCheckpoint(long commit, long prepare) {
        if (this.checkpointer == null)
            return;

        checkpointer.onCheckpoint(this.subscription, new Position(commit, prepare));
    }

    @Override
    public void onStreamNotFound(String streamName) {}

    @Override
    public void onFirstStreamPosition(long position) {}

    @Override
    public void onLastStreamPosition(long position) {}

    @Override
    public void onLastAllStreamPosition(long commit, long prepare) {}

    @Override
    public void onCaughtUp(Instant timestamp, Long streamRevision, Position position) {
        this.listener.onCaughtUp(this.subscription, timestamp, streamRevision, position);
    }

    @Override
    public void onFellBehind(Instant timestamp, Long streamRevision, Position position) {
        this.listener.onFellBehind(this.subscription, timestamp, streamRevision, position);
    }

    @Override
    public void onCancelled(Throwable exception) {
        // error occurred before subscription was confirmed
        if (this.subscription == null) {
            this.future.completeExceptionally(exception);
        }

        this.listener.onCancelled(this.subscription, exception);
    }

    @Override
    public void onComplete() {
    }
}
