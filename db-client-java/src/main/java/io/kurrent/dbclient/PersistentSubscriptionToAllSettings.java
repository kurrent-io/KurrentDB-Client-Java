package io.kurrent.dbclient;

/**
 * Persistent subscription to $all settings.
 */
public class PersistentSubscriptionToAllSettings extends PersistentSubscriptionSettings {
    private StreamPosition<Position> startFrom;
    PersistentSubscriptionToAllSettings() {}

    /**
     * Return a persistent subscription settings to $all with default properties.
     */
    public static PersistentSubscriptionToAllSettings get() {
        return PersistentSubscriptionSettings.defaultToAll();
    }

    /**
     * Where to start subscription from. This can be from the start of the $all stream, from the end of the $all stream
     * at the time of creation, or from an inclusive position in $all stream.
     */
    public StreamPosition<Position> getStartFrom() {
        return startFrom;
    }

    /**
     * Where to start subscription from. This can be from the start of the $all stream, from the end of the $all stream
     * at the time of creation, or from an inclusive position in $all stream.
     */
    void setStartFrom(StreamPosition<Position> startFrom) {
        this.startFrom = startFrom;
    }

    @Override
    public String toString() {
        return "PersistentSubscriptionToAllSettings{" +
                "startFrom=" + startFrom +
                ", checkpointAfterInMs=" + getCheckpointAfterInMs() +
                ", extraStatistics=" + isExtraStatistics() +
                ", shouldResolveLinkTos=" + shouldResolveLinkTos() +
                ", historyBufferSize=" + getHistoryBufferSize() +
                ", liveBufferSize=" + getLiveBufferSize() +
                ", checkpointUpperBound=" + getCheckpointUpperBound() +
                ", maxRetryCount=" + getMaxRetryCount() +
                ", maxSubscriberCount=" + getMaxSubscriberCount() +
                ", messageTimeoutMs=" + getMessageTimeoutMs() +
                ", checkpointLowerBound=" + getCheckpointLowerBound() +
                ", readBatchSize=" + getReadBatchSize() +
                ", namedConsumerStrategy=" + getNamedConsumerStrategy() +
                ", resolveLinkTos=" + isResolveLinkTos() +
                '}';
    }
}
