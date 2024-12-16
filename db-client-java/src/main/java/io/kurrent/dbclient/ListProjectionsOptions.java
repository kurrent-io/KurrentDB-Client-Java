package io.kurrent.dbclient;

/**
 * Options of the list projections options.
 */
public class ListProjectionsOptions extends OptionsBase<ListProjectionsOptions> {
    private ListProjectionsOptions() {
    }

    /**
     * Returns options with default values.
     */
    public static ListProjectionsOptions get() {
        return new ListProjectionsOptions();
    }
}
