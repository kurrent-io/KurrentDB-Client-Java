package io.kurrent.dbclient;

/**
 * Options of create persistent subscription to $all request.
 */
public class CreatePersistentSubscriptionToAllOptions
        extends AbstractPersistentSubscriptionSettingsBuilder<CreatePersistentSubscriptionToAllOptions, PersistentSubscriptionToAllSettings>  {

    private SubscriptionFilter filter;

    CreatePersistentSubscriptionToAllOptions() {
        super(PersistentSubscriptionSettings.defaultToAll());
    }

    /**
     * Returns options with default values.
     */
    public static CreatePersistentSubscriptionToAllOptions get() {
        return new CreatePersistentSubscriptionToAllOptions();
    }

    SubscriptionFilter getFilter() {
        return filter;
    }

    /**
     * Applies a server-side filter to determine if an event of the subscription should be yielded.
     */
    public CreatePersistentSubscriptionToAllOptions filter(SubscriptionFilter filter) {
        this.filter = filter;
        return this;
    }

    /**
     * Starts the subscription from the beginning of the $all stream.
     */
    public CreatePersistentSubscriptionToAllOptions fromStart() {
        getSettings().setStartFrom(StreamPosition.start());
        return this;
    }

    /**
     * Starts the subscription from the end of the $all stream.
     */
    public CreatePersistentSubscriptionToAllOptions fromEnd() {
        getSettings().setStartFrom(StreamPosition.end());
        return this;
    }

    /**
     * Starts the subscription from the given transaction log position.
     * @param position a transaction log position.
     * @see Position
     */
    public CreatePersistentSubscriptionToAllOptions startFrom(Position position) {
        getSettings().setStartFrom(StreamPosition.position(position));
        return this;
    }

    /**
     * Starts the subscription from the given transaction log position.
     * @see Position
     */
    public CreatePersistentSubscriptionToAllOptions startFrom(long unsignedPrepare, long unsignedCommit) {
        getSettings().setStartFrom(StreamPosition.position(new Position(unsignedCommit, unsignedPrepare)));
        return this;
    }
}
