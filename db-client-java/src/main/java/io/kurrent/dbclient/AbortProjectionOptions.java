package io.kurrent.dbclient;

/**
 * Options of the abort projection request.
 */
public class AbortProjectionOptions extends OptionsBase<AbortProjectionOptions> {
    private AbortProjectionOptions() {
    }

    /**
     * Returns options with default values.
     * @return options
     */
    public static AbortProjectionOptions get() {
        return new AbortProjectionOptions();
    }
}
