package io.kurrent.dbclient;

/**
 * Options of the get projection state request.
 */
public class GetProjectionStateOptions extends OptionsBase<GetProjectionStateOptions> {
    private String partition;

    private GetProjectionStateOptions() {
        this.partition = "";
    }

    /**
     * Returns options with default values.
     */
    public static GetProjectionStateOptions get() {
        return new GetProjectionStateOptions();
    }

    /**
     * Specifies which partition to retrieve the state from.
     */
    public GetProjectionStateOptions partition(String partition) {
        this.partition = partition;
        return this;
    }

    String getPartition() {
        return this.partition;
    }
}
