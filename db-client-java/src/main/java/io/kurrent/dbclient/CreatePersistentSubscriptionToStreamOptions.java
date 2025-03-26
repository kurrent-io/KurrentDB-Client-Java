package io.kurrent.dbclient;

/**
 * Options for the create persistent subscription to stream request.
 */
public class CreatePersistentSubscriptionToStreamOptions
        extends AbstractPersistentSubscriptionSettingsBuilder<CreatePersistentSubscriptionToStreamOptions, PersistentSubscriptionToStreamSettings> {
    CreatePersistentSubscriptionToStreamOptions() {
        super(PersistentSubscriptionSettings.defaultRegular());
    }

    /**
     * Returns options with default values.
     */
    public static CreatePersistentSubscriptionToStreamOptions get() {
        return new CreatePersistentSubscriptionToStreamOptions();
    }

    /**
     * Starts the subscription from the beginning of the given stream.

     */
    public CreatePersistentSubscriptionToStreamOptions fromStart() {
        getSettings().setStartFrom(StreamPosition.start());
        return this;
    }

    /**
     * Starts the subscription from the end of the given stream.

     */
    public CreatePersistentSubscriptionToStreamOptions fromEnd() {
        getSettings().setStartFrom(StreamPosition.end());
        return this;
    }

    /**
     * Starts the subscription from the given stream revision.
     */
    public CreatePersistentSubscriptionToStreamOptions startFrom(long revision) {
        getSettings().setStartFrom(StreamPosition.position(revision));
        return this;
    }
}
