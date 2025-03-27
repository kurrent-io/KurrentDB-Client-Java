package io.kurrent.dbclient;

/**
 * Options of the list persistent subscriptions request.
 */
public class ListPersistentSubscriptionsOptions extends OptionsBase<ListPersistentSubscriptionsOptions> {
    ListPersistentSubscriptionsOptions(){}

    /**
     * Returns options with default values.
     */
    public static ListPersistentSubscriptionsOptions get() {
        return new ListPersistentSubscriptionsOptions();
    }
}
