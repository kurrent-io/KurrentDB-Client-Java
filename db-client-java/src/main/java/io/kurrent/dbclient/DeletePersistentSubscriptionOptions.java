package io.kurrent.dbclient;

/**
 * Options of the delete persistent subscription request.
 */
public class DeletePersistentSubscriptionOptions extends OptionsBase<DeletePersistentSubscriptionOptions> {
    private DeletePersistentSubscriptionOptions() {
    }

    /**
     * Returns options with default values.
     */
    public static DeletePersistentSubscriptionOptions get() {
        return new DeletePersistentSubscriptionOptions();
    }
}
