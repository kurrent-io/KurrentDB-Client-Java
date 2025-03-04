package com.eventstore.dbclient;

/**
 * Options of the append stream request.
 */
public class AppendToStreamOptions extends OptionsWithStreamStateBase<AppendToStreamOptions> {
    private AppendToStreamOptions() {
    }

    /**
     * Returns options with default values.
     */
    public static AppendToStreamOptions get() {
        return new AppendToStreamOptions();
    }
}
