package io.kurrent.dbclient;

/**
 * Options of the get projection result request.
 */
public class GetProjectionResultOptions extends OptionsBase<GetProjectionResultOptions> {
    private String partition;

    public GetProjectionResultOptions() {
        this.partition = "";
    }

    /**
     * Returns options with default values.
     */
    public static GetProjectionResultOptions get() {
        return new GetProjectionResultOptions();
    }

   String getPartition() {
        return this.partition;
   }

    /**
     * Specifies which partition to retrieve the result from.
     */
   public GetProjectionResultOptions partition(String partition) {
       this.partition = partition;
       return this;
   }
}
