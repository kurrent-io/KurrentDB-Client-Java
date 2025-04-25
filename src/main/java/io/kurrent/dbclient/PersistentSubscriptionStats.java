package io.kurrent.dbclient;

/**
 * Processing-related persistent subscription statistics.
 */
public abstract class PersistentSubscriptionStats {
    private int averagePerSecond;
    private long totalItems;
    private int countSinceLastMeasurement;
    private int readBufferCount;
    private long liveBufferCount;
    private int retryBufferCount;
    private int totalInFlightMessages;
    private int outstandingMessagesCount;
    private long parkedMessageCount;

    PersistentSubscriptionStats(){}

    /**
     * Average number of events per seconds.
     */
    public int getAveragePerSecond() {
        return averagePerSecond;
    }

    void setAveragePerSecond(int averagePerSecond) {
        this.averagePerSecond = averagePerSecond;
    }

    /**
     * Total number of events processed by subscription.
     */
    public long getTotalItems() {
        return totalItems;
    }

    void setTotalItems(long totalItems) {
        this.totalItems = totalItems;
    }

    /**
     * Number of events seen since last measurement on this connection. Used as the basis for <i>averagePerSecond</i>
     */
    public int getCountSinceLastMeasurement() {
        return countSinceLastMeasurement;
    }

    void setCountSinceLastMeasurement(int countSinceLastMeasurement) {
        this.countSinceLastMeasurement = countSinceLastMeasurement;
    }

    /**
     * Number of events in the read buffer.
     */
    public int getReadBufferCount() {
        return readBufferCount;
    }

    void setReadBufferCount(int readBufferCount) {
        this.readBufferCount = readBufferCount;
    }

    /**
     * Number of events in the live buffer.
     */
    public long getLiveBufferCount() {
        return liveBufferCount;
    }

    void setLiveBufferCount(long liveBufferCount) {
        this.liveBufferCount = liveBufferCount;
    }

    /**
     * Number of events in the retry buffer.
     */
    public int getRetryBufferCount() {
        return retryBufferCount;
    }

    void setRetryBufferCount(int retryBufferCount) {
        this.retryBufferCount = retryBufferCount;
    }

    /**
     * Current in flight messages across the persistent subscription group.
     */
    public int getTotalInFlightMessages() {
        return totalInFlightMessages;
    }

    void setTotalInFlightMessages(int totalInFlightMessages) {
        this.totalInFlightMessages = totalInFlightMessages;
    }

    /**
     * Current number of outstanding messages.
     */
    public int getOutstandingMessagesCount() {
        return outstandingMessagesCount;
    }

    void setOutstandingMessagesCount(int outstandingMessagesCount) {
        this.outstandingMessagesCount = outstandingMessagesCount;
    }

    /**
     * The current number of parked messages.
     */
    public long getParkedMessageCount() {
        return parkedMessageCount;
    }

    void setParkedMessageCount(long parkedMessageCount) {
        this.parkedMessageCount = parkedMessageCount;
    }
}
