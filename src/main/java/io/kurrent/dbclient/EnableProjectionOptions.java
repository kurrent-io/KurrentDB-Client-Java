package io.kurrent.dbclient;

/**
 * Options of the enable projection request.
 */
public class EnableProjectionOptions extends OptionsBase<EnableProjectionOptions> {
    private EnableProjectionOptions() {
    }

    /**
     * Returns options with default values.
     */
    public static EnableProjectionOptions get() {
        return new EnableProjectionOptions();
    }
}
