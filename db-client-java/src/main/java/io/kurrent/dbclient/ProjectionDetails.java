package io.kurrent.dbclient;

import com.eventstore.dbclient.proto.projections.Projectionmanagement;

/**
 * Provides the details for a projection.
 */
public class ProjectionDetails {
    private final long coreProcessingTime;
    private final long version;
    private final long epoch;
    private final String effectiveName;
    private final int writesInProgress;
    private final int readsInProgress;
    private final int partitionsCached;
    private final String status;
    private final String stateReason;
    private final String name;
    private final String mode;
    private final String position;
    private final float progress;
    private final String lastCheckpoint;
    private final long eventsProcessedAfterRestart;
    private final String checkpointStatus;
    private final long bufferedEvents;
    private final int writePendingEventsBeforeCheckpoint;
    private final int writePendingEventsAfterCheckpoint;

    ProjectionDetails(long coreProcessingTime, long version, long epoch, String effectiveName,
                             int writesInProgress, int readsInProgress, int partitionsCached, String status,
                             String stateReason, String name, String mode, String position, float progress,
                             String lastCheckpoint, long eventsProcessedAfterRestart, String checkpointStatus,
                             long bufferedEvents, int writePendingEventsBeforeCheckpoint,
                             int writePendingEventsAfterCheckpoint) {

        this.coreProcessingTime = coreProcessingTime;
        this.version = version;
        this.epoch = epoch;
        this.effectiveName = effectiveName;
        this.writesInProgress = writesInProgress;
        this.readsInProgress = readsInProgress;
        this.partitionsCached = partitionsCached;
        this.status = status;
        this.stateReason = stateReason;
        this.name = name;
        this.mode = mode;
        this.position = position;
        this.progress = progress;
        this.lastCheckpoint = lastCheckpoint;
        this.eventsProcessedAfterRestart = eventsProcessedAfterRestart;
        this.checkpointStatus = checkpointStatus;
        this.bufferedEvents = bufferedEvents;
        this.writePendingEventsBeforeCheckpoint = writePendingEventsBeforeCheckpoint;
        this.writePendingEventsAfterCheckpoint = writePendingEventsAfterCheckpoint;
    }

    static ProjectionDetails fromWire(Projectionmanagement.StatisticsResp.Details details) {
        return new ProjectionDetails(
            details.getCoreProcessingTime(),
            details.getVersion(),
            details.getEpoch(),
            details.getEffectiveName(),
            details.getWritesInProgress(),
            details.getReadsInProgress(),
            details.getPartitionsCached(),
            details.getStatus(),
            details.getStateReason(),
            details.getName(),
            details.getMode(),
            details.getPosition(),
            details.getProgress(),
            details.getLastCheckpoint(),
            details.getEventsProcessedAfterRestart(),
            details.getCheckpointStatus(),
            details.getBufferedEvents(),
            details.getWritePendingEventsBeforeCheckpoint(),
            details.getWritePendingEventsAfterCheckpoint()
        );
    }

    /**
     * The core processing time.
     */
    public long getCoreProcessingTime() {
        return coreProcessingTime;
    }

    /**
     * The projection version.
     */
    public long getVersion() {
        return version;
    }

    /**
     * The projection's current epoch.
     */
    public long getEpoch() {
        return epoch;
    }

    /**
     * The projection's effective name.
     */
    public String getEffectiveName() {
        return effectiveName;
    }

    /**
     * The projection's writes-in-progress.
     */
    public int getWritesInProgress() {
        return writesInProgress;
    }

    /**
     * The projection's reads-in-progress.
     */
    public int getReadsInProgress() {
        return readsInProgress;
    }

    /**
     * The number of partitions cached.
     */
    public int getPartitionsCached() {
        return partitionsCached;
    }

    /**
     * The projection's status.
     */
    public String getStatus() {
        return status;
    }

    /**
     * The projection's StateReason.
     */
    public String getStateReason() {
        return stateReason;
    }

    /**
     * The projection's name.
     */
    public String getName() {
        return name;
    }

    /**
     * The projection's mode.
     */
    public String getMode() {
        return mode;
    }

    /**
     * The projection's position.
     */
    public String getPosition() {
        return position;
    }

    /**
     * The projection's progress.
     */
    public float getProgress() {
        return progress;
    }

    /**
     * The projection's last checkpoint.
     */
    public String getLastCheckpoint() {
        return lastCheckpoint;
    }

    /**
     * The projection's events processed after restart.
     */
    public long getEventsProcessedAfterRestart() {
        return eventsProcessedAfterRestart;
    }

    /**
     * The projection's checkpoint status.
     */
    public String getCheckpointStatus() {
        return checkpointStatus;
    }

    /**
     * The projection's buffered events.
     */
    public long getBufferedEvents() {
        return bufferedEvents;
    }

    /**
     * The projection write pending events before checkpoint.
     */
    public int getWritePendingEventsBeforeCheckpoint() {
        return writePendingEventsBeforeCheckpoint;
    }

    /**
     * The projection write pending events after checkpoint.
     */
    public int getWritePendingEventsAfterCheckpoint() {
        return writePendingEventsAfterCheckpoint;
    }

    @Override
    public String toString() {
        return "ProjectionDetails{" +
                "coreProcessingTime=" + coreProcessingTime +
                ", version=" + version +
                ", epoch=" + epoch +
                ", effectiveName='" + effectiveName + '\'' +
                ", writesInProgress=" + writesInProgress +
                ", readsInProgress=" + readsInProgress +
                ", partitionsCached=" + partitionsCached +
                ", status='" + status + '\'' +
                ", stateReason='" + stateReason + '\'' +
                ", name='" + name + '\'' +
                ", mode='" + mode + '\'' +
                ", position='" + position + '\'' +
                ", progress=" + progress +
                ", lastCheckpoint='" + lastCheckpoint + '\'' +
                ", eventsProcessedAfterRestart=" + eventsProcessedAfterRestart +
                ", checkpointStatus='" + checkpointStatus + '\'' +
                ", bufferedEvents=" + bufferedEvents +
                ", writePendingEventsBeforeCheckpoint=" + writePendingEventsBeforeCheckpoint +
                ", writePendingEventsAfterCheckpoint=" + writePendingEventsAfterCheckpoint +
                '}';
    }
}
