package io.kurrent.dbclient;

import java.util.Map;

/**
 * Holds a persistent subscription connection info.
 */
public class PersistentSubscriptionConnectionInfo {
    private String from;
    private String username;
    private int averageItemsPerSecond;
    private long totalItems;
    private long countSinceLastMeasurement;
    private int availableSlots;
    private int inFlightMessages;
    private String connectionName;
    private Map<String, Long> extraStatistics;

    PersistentSubscriptionConnectionInfo() {}

    /**
     * Origin of this connection.
     */
    public String getFrom() {
        return from;
    }

    void setFrom(String from) {
        this.from = from;
    }

    /**
     * Connection's username.
     */
    public String getUsername() {
        return username;
    }

    void setUsername(String username) {
        this.username = username;
    }

    /**
     * Average events per second on this connection.
     */
    public double getAverageItemsPerSecond() {
        return averageItemsPerSecond;
    }

    void setAverageItemsPerSecond(int averageItemsPerSecond) {
        this.averageItemsPerSecond = averageItemsPerSecond;
    }

    /**
     * Total items on this connection.
     */
    public long getTotalItems() {
        return totalItems;
    }

    void setTotalItems(long totalItems) {
        this.totalItems = totalItems;
    }

    /**
     * Number of items seen since last measurement on this connection. Used as the basis for
     * <i>averageItemsPerSecond</i>
     */
    public long getCountSinceLastMeasurement() {
        return countSinceLastMeasurement;
    }

    void setCountSinceLastMeasurement(long countSinceLastMeasurement) {
        this.countSinceLastMeasurement = countSinceLastMeasurement;
    }

    /**
     * Number of available slots.
     */
    public long getAvailableSlots() {
        return availableSlots;
    }

    void setAvailableSlots(int availableSlots) {
        this.availableSlots = availableSlots;
    }

    /**
     * Number of in flight messages on this connection.
     */
    public long getInFlightMessages() {
        return inFlightMessages;
    }

    void setInFlightMessages(int inFlightMessages) {
        this.inFlightMessages = inFlightMessages;
    }

    /**
     * Connection name.
     */
    public String getConnectionName() {
        return connectionName;
    }

    void setConnectionName(String connectionName) {
        this.connectionName = connectionName;
    }

    /**
     * Timing measurements for the connection. Can be enabled with the <i>extraStatistics</i> setting.
     */
    public Map<String, Long> getExtraStatistics() {
        return extraStatistics;
    }

    void setExtraStatistics(Map<String, Long> extraStatistics) {
        this.extraStatistics = extraStatistics;
    }

    @Override
    public String toString() {
        return "PersistentSubscriptionConnectionInfo{" +
                "from='" + from + '\'' +
                ", username='" + username + '\'' +
                ", averageItemsPerSecond=" + averageItemsPerSecond +
                ", totalItems=" + totalItems +
                ", countSinceLastMeasurement=" + countSinceLastMeasurement +
                ", availableSlots=" + availableSlots +
                ", inFlightMessages=" + inFlightMessages +
                ", connectionName='" + connectionName + '\'' +
                ", extraStatistics=" + extraStatistics +
                '}';
    }
}
