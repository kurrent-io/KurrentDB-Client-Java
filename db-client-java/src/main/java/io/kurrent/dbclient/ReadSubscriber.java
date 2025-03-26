package io.kurrent.dbclient;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

abstract class ReadSubscriber implements Subscriber<ReadMessage> {

    private Subscription subscription;

    @Override
    public final void onSubscribe(Subscription s) {
        this.subscription = s;
    }

    public final void request(long n) {
        this.subscription.request(n);
    }

    @Override
    public final void onNext(ReadMessage resolvedEvent) {
        onEvent(resolvedEvent);
    }

    public abstract void onEvent(ReadMessage resolvedEvent);
}
