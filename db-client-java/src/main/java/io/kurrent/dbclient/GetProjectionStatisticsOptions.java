package io.kurrent.dbclient;

/**
 * Options of the get projection statistics request.
 */
public class GetProjectionStatisticsOptions extends OptionsBase<GetProjectionStatisticsOptions> {
    private GetProjectionStatisticsOptions() {
    }

    /**
     * Returns options with default values.
     */
    public static GetProjectionStatisticsOptions get() {
        return new GetProjectionStatisticsOptions();
    }
}
