package io.kurrent.dbclient;

import java.util.Optional;

/**
 * Processing-related persistent subscription to stream statistics.
 */
public class PersistentSubscriptionToStreamStats extends PersistentSubscriptionStats {
    private Long lastCheckpointedEventRevision;
    private Long lastKnownEventRevision;

    /**
     * The revision number of the last checkpoint.
     */
    public Optional<Long> getLastCheckpointedEventRevision() {
        if (lastCheckpointedEventRevision == null)
            return Optional.empty();

        return Optional.of(lastCheckpointedEventRevision);
    }

    void setLastCheckpointedEventRevision(long lastCheckpointedEventRevision) {
        this.lastCheckpointedEventRevision = lastCheckpointedEventRevision;
    }

    /**
     * The revision number of the last known event.
     */
    public Optional<Long> getLastKnownEventRevision() {
        if (lastKnownEventRevision == null)
            return Optional.empty();

        return Optional.of(lastKnownEventRevision);
    }

    void setLastKnownEventRevision(long lastKnownEventRevision) {
        this.lastKnownEventRevision = lastKnownEventRevision;
    }

    @Override
    public String toString() {
        return "PersistentSubscriptionToStreamStats{" +
                "lastCheckpointedEventRevision=" + lastCheckpointedEventRevision +
                ", lastKnownEventRevision=" + lastKnownEventRevision +
                ", averagePerSecond=" + getAveragePerSecond() +
                ", totalItems=" + getTotalItems() +
                ", countSinceLastMeasurement=" + getCountSinceLastMeasurement() +
                ", readBufferCount=" + getReadBufferCount() +
                ", liveBufferCount=" + getLiveBufferCount() +
                ", retryBufferCount=" + getRetryBufferCount() +
                ", totalInFlightMessages=" + getTotalInFlightMessages() +
                ", outstandingMessagesCount=" + getOutstandingMessagesCount() +
                ", parkedMessageCount=" + getParkedMessageCount() +
                '}';
    }
}
