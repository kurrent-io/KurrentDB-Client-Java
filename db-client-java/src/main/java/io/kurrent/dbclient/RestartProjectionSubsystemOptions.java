package io.kurrent.dbclient;

/**
 * Options of the restart projection subsystem request.
 */
public class RestartProjectionSubsystemOptions extends OptionsBase<RestartProjectionSubsystemOptions> {
    private RestartProjectionSubsystemOptions() {
    }

    /**
     * Options with default values.
     */
    public static RestartProjectionSubsystemOptions get() {
        return new RestartProjectionSubsystemOptions();
    }
}
