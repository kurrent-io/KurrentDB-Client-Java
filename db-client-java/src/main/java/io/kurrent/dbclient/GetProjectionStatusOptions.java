package io.kurrent.dbclient;

/**
 * Options of the get projection status request.
 */
public class GetProjectionStatusOptions extends OptionsBase<GetProjectionStatusOptions> {
    private GetProjectionStatusOptions() {
    }

    /**
     * Returns options with default values.
     */
    public static GetProjectionStatusOptions get() {
        return new GetProjectionStatusOptions();
    }
}
