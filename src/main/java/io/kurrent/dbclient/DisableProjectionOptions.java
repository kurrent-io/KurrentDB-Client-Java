package io.kurrent.dbclient;

/**
 * Options of the disable projection request.
 */
public class DisableProjectionOptions extends OptionsBase<DisableProjectionOptions> {
    private DisableProjectionOptions() {
    }

    /**
     * Returns options with default values.
     */
    public static DisableProjectionOptions get() {
        return new DisableProjectionOptions();
    }
}
