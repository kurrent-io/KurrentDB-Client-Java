package com.eventstore.dbclient;

/**
 * Listener used to handle persistent subscription notifications raised throughout its lifecycle.
 */
public abstract class PersistentSubscriptionListener {
    /**
     * Called when KurrentDB sends an event to the persistent subscription.
     * @param subscription handle to the persistent subscription.
     * @param retryCount how many times the event was retried.
     * @param event a resolved event.
     */
    public void onEvent(PersistentSubscription subscription, int retryCount, ResolvedEvent event) {}

    /**
     * Called when the subscription is cancelled or dropped.
     * @param subscription handle to the subscription.
     * @param exception an exception. null if the user initiated the cancellation.
     */
    public void onCancelled(PersistentSubscription subscription, Throwable exception) {}

    /**
     * Called when the subscription is confirmed by the server.
     * @param subscription handle to the subscription.
     */
    public void onConfirmation(PersistentSubscription subscription) {}
}