package io.kurrent.dbclient;

/**
 * Options of the append stream request.
 */
public class AppendToStreamOptions extends OptionsWithExpectedRevisionBase<AppendToStreamOptions> {
    private AppendToStreamOptions() {
    }

    /**
     * Returns options with default values.
     */
    public static AppendToStreamOptions get() {
        return new AppendToStreamOptions();
    }
}
