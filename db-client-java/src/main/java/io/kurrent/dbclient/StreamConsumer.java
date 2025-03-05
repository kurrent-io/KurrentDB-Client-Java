package com.eventstore.dbclient;

public interface StreamConsumer {
    default void onSubscribe(org.reactivestreams.Subscription subscription) {}
    void onEvent(ResolvedEvent event);
    void onSubscriptionConfirmation(String subscriptionId);
    void onCheckpoint(long commit, long prepare);
    void onStreamNotFound(String streamName);
    void onFirstStreamPosition(long position);
    void onLastStreamPosition(long position);
    void onLastAllStreamPosition(long commit, long prepare);
    void onCaughtUp();
    void onFellBehind();
    void onCancelled(Throwable exception);
    void onComplete();
}
