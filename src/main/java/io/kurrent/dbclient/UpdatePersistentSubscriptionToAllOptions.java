package io.kurrent.dbclient;

/**
 * Options of the update persistent subscription to $all stream request.
 */
public class UpdatePersistentSubscriptionToAllOptions
        extends AbstractPersistentSubscriptionSettingsBuilder<UpdatePersistentSubscriptionToAllOptions, PersistentSubscriptionToAllSettings> {
    UpdatePersistentSubscriptionToAllOptions() {
        this(PersistentSubscriptionSettings.defaultToAll());
    }

    UpdatePersistentSubscriptionToAllOptions(PersistentSubscriptionToAllSettings settings) {
        super(settings);
    }

    /**
     * Returns options with default values.
     */
    public static UpdatePersistentSubscriptionToAllOptions get() {
        return new UpdatePersistentSubscriptionToAllOptions();
    }

    /**
     * Returns options from a persistent subscription to $all settings.
     * @see PersistentSubscriptionToAllSettings
     */
    public static UpdatePersistentSubscriptionToAllOptions from(PersistentSubscriptionToAllSettings settings) {
        return new UpdatePersistentSubscriptionToAllOptions(settings);
    }

    /**
     * Starts the persistent subscription from the beginning of the $all stream.
     */
    public UpdatePersistentSubscriptionToAllOptions fromStart() {
        getSettings().setStartFrom(StreamPosition.start());
        return this;
    }

    /**
     * Starts the persistent subscription from the end of the $all stream.
     */
    public UpdatePersistentSubscriptionToAllOptions fromEnd() {
        getSettings().setStartFrom(StreamPosition.end());
        return this;
    }

    /**
     * Starts the persistent subscription from a specific transaction log position.
     */
    public UpdatePersistentSubscriptionToAllOptions startFrom(Position position) {
        getSettings().setStartFrom(StreamPosition.position(position));
        return this;
    }

    /**
     * Starts the persistent subscription from a specific transaction log position.
     */
    public UpdatePersistentSubscriptionToAllOptions startFrom(long unsignedPrepare, long unsignedCommit) {
        getSettings().setStartFrom(StreamPosition.position(new Position(unsignedCommit, unsignedPrepare)));
        return this;
    }
}
