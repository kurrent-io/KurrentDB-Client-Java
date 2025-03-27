package io.kurrent.dbclient;

import io.kurrent.dbclient.proto.streams.StreamsOuterClass;
import io.grpc.stub.ClientCallStreamObserver;

/**
 * Subscription handle.
 */
public class Subscription {
    private final org.reactivestreams.Subscription internal;
    private final String subscriptionId;
    private final Checkpointer checkpointer;

    Subscription(org.reactivestreams.Subscription internal, String subscriptionId, Checkpointer checkpointer) {
        this.internal = internal;
        this.subscriptionId = subscriptionId;
        this.checkpointer = checkpointer;
    }

    /**
     * Returns subscription's id.
     */
    public String getSubscriptionId() {
        return this.subscriptionId;
    }

    /**
     * Drops the subscription.
     */
    public void stop() {
        this.internal.cancel();
    }

    Checkpointer getCheckpointer() {
        return this.checkpointer;
    }
}
