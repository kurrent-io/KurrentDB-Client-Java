package io.kurrent.dbclient;

/**
 * Options of the delete or tombstone stream request.
 */
public class DeleteStreamOptions extends OptionsWithExpectedRevisionBase<DeleteStreamOptions> {
    DeleteStreamOptions() {}

    /**
     * Returns options with default values.
     */
    public static DeleteStreamOptions get() {
        return new DeleteStreamOptions();
    }
}
