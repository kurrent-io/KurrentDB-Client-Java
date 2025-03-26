package io.kurrent.dbclient;

/**
 * Options of the update persistent subscription to stream request.
 */
public class UpdatePersistentSubscriptionToStreamOptions
        extends AbstractPersistentSubscriptionSettingsBuilder<UpdatePersistentSubscriptionToStreamOptions, PersistentSubscriptionToStreamSettings>  {
    UpdatePersistentSubscriptionToStreamOptions() {
        this(PersistentSubscriptionSettings.defaultRegular());
    }

    UpdatePersistentSubscriptionToStreamOptions(PersistentSubscriptionToStreamSettings settings) {
        super(settings);
    }

    /**
     * Returns options with default values.
     */
    public static UpdatePersistentSubscriptionToStreamOptions get() {
        return new UpdatePersistentSubscriptionToStreamOptions();
    }

    /**
     * Returns options from a persistent subscription to stream settings.
     * @see PersistentSubscriptionToStreamSettings
     */
    public static UpdatePersistentSubscriptionToStreamOptions from(PersistentSubscriptionToStreamSettings settings) {
        return new UpdatePersistentSubscriptionToStreamOptions(settings);
    }

    /**
     * Starts the persistent subscription from the beginning of the stream.
     */
    public UpdatePersistentSubscriptionToStreamOptions fromStart() {
        getSettings().setStartFrom(StreamPosition.start());
        return this;
    }

    /**
     * Starts the persistent subscription from the end of the stream.
     */
    public UpdatePersistentSubscriptionToStreamOptions fromEnd() {
        getSettings().setStartFrom(StreamPosition.end());
        return this;
    }

    /**
     * Starts the persistent subscription from a specific revision number.
     */
    public UpdatePersistentSubscriptionToStreamOptions startFrom(long revision) {
        getSettings().setStartFrom(StreamPosition.position(revision));
        return this;
    }
}
