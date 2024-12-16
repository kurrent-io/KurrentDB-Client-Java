package io.kurrent.dbclient;

/**
 * Options of the reset projection request.
 */
public class ResetProjectionOptions extends OptionsBase<ResetProjectionOptions> {
    private ResetProjectionOptions() {
    }

    /**
     * Options with default values.
     */
    public static ResetProjectionOptions get() {
        return new ResetProjectionOptions();
    }
}
