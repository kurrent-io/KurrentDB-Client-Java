package io.kurrent.dbclient;

/**
 * Options of the restart persistent subscription subsystem request.
 */
public class RestartPersistentSubscriptionSubsystemOptions extends OptionsBase<RestartPersistentSubscriptionSubsystemOptions> {
    RestartPersistentSubscriptionSubsystemOptions(){}

    /**
     * Options with the default values.
     */
    public static RestartPersistentSubscriptionSubsystemOptions get() {
        return new RestartPersistentSubscriptionSubsystemOptions();
    }
}
