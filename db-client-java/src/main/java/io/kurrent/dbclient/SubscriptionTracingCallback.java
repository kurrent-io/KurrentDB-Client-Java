package io.kurrent.dbclient;

@FunctionalInterface
public interface SubscriptionTracingCallback {
    void trace(String subscriptionId, RecordedEvent event, Runnable action);
}
