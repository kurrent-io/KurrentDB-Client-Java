package io.kurrent.dbclient;

/**
 * Listener used to handle catch-up subscription notifications raised throughout its lifecycle.
 */
public abstract class SubscriptionListener {
    /**
     * Called when EventStoreDB sends an event to the subscription.
     * @param subscription handle to the subscription.
     * @param event a resolved event.
     */
    public void onEvent(Subscription subscription, ResolvedEvent event) {}

    /**
     * Called when the subscription is cancelled or dropped.
     * @param subscription handle to the subscription.
     * @param exception an exception. null if the user initiated the cancellation.
     */
    public void onCancelled(Subscription subscription, Throwable exception) {}

    /**
     * Called when the subscription is confirmed by the server.
     * @param subscription handle to the subscription.
     */
    public void onConfirmation(Subscription subscription) {}

    /**
     * Called when the subscription has reached the head of the stream.
     * @param subscription handle to the subscription.
     */
    public void onCaughtUp(Subscription subscription) {}

    /**
     * Called when the subscription has fallen behind, meaning it's no longer keeping up with the
     * stream's pace.
     * @param subscription handle to the subscription.
     */
    public void onFellBehind(Subscription subscription) {}
}
