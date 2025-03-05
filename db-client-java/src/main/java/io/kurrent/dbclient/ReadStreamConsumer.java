package com.eventstore.dbclient;

import org.reactivestreams.Subscriber;

class ReadStreamConsumer implements StreamConsumer {
    private final Subscriber<? super ReadMessage> subscriber;

    public ReadStreamConsumer(Subscriber<? super ReadMessage> subscriber) {
        this.subscriber = subscriber;
    }

    @Override
    public void onEvent(ResolvedEvent event) {
        this.subscriber.onNext(ReadMessage.fromEvent(event));
    }

    @Override
    public void onSubscriptionConfirmation(String subscriptionId) {}

    @Override
    public void onCheckpoint(long commit, long prepare) {}

    @Override
    public void onStreamNotFound(String streamName) {
        this.subscriber.onError(new StreamNotFoundException(streamName));
    }

    @Override
    public void onFirstStreamPosition(long position) {
        this.subscriber.onNext(ReadMessage.fromFirstStreamPosition(position));
    }

    @Override
    public void onLastStreamPosition(long position) {
        this.subscriber.onNext(ReadMessage.fromLastStreamPosition(position));
    }

    @Override
    public void onLastAllStreamPosition(long commit, long prepare) {
        this.subscriber.onNext(ReadMessage.fromLastAllPosition(commit, prepare));
    }

    @Override
    public void onCaughtUp() {}

    @Override
    public void onFellBehind() {}

    @Override
    public void onCancelled(Throwable exception) {
        this.subscriber.onError(exception);
    }

    @Override
    public void onComplete() {
        this.subscriber.onComplete();
    }
}
