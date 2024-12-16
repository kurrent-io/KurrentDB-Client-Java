package io.kurrent.dbclient;

import java.util.List;

/**
 * Common persistent subscription info type.
 */
public abstract class PersistentSubscriptionInfo {
    private String eventSource;
    private String groupName;
    private String status;
    private List<PersistentSubscriptionConnectionInfo> connections;

    PersistentSubscriptionInfo(){}

    /**
     * The source of events for the subscription.
     */
    public String getEventSource() {
        return eventSource;
    }

    void setEventSource(String eventSource) {
        this.eventSource = eventSource;
    }

    /**
     * The group name given on creation.
     */
    public String getGroupName() {
        return groupName;
    }

    void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    /**
     * The current status of the subscription.
     */
    public String getStatus() {
        return status;
    }

    void setStatus(String status) {
        this.status = status;
    }

    /**
     * Active connections to the subscription.
     * @see PersistentSubscriptionConnectionInfo
     */
    public List<PersistentSubscriptionConnectionInfo> getConnections() {
        return connections;
    }

    void setConnections(List<PersistentSubscriptionConnectionInfo> connections) {
        this.connections = connections;
    }
}
