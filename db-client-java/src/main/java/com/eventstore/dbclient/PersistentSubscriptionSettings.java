package com.eventstore.dbclient;

import java.time.Duration;

/**
 * Common persistent subscription settings type.
 */
public abstract class PersistentSubscriptionSettings {
    private int checkpointAfter;
    private boolean extraStatistics;
    private boolean resolveLinkTos;
    private int historyBufferSize;
    private int liveBufferSize;
    private int checkpointUpperBound;
    private int maxRetryCount;
    private int maxSubscriberCount;
    private int messageTimeoutMs;
    private int checkpointLowerBound;
    private int readBatchSize;
    private NamedConsumerStrategy namedConsumerStrategy;

    PersistentSubscriptionSettings() {}

    static PersistentSubscriptionToStreamSettings defaultRegular() {
        PersistentSubscriptionToStreamSettings settings = new PersistentSubscriptionToStreamSettings();
        defaultCommon(settings);

        settings.setStartFrom(StreamPosition.end());

        return settings;
    }

    static PersistentSubscriptionToAllSettings defaultToAll() {
        PersistentSubscriptionToAllSettings settings = new PersistentSubscriptionToAllSettings();
        defaultCommon(settings);

        settings.setStartFrom(StreamPosition.end());

        return settings;
    }

    private static <A extends PersistentSubscriptionSettings> void defaultCommon(A settings) {
        settings.setMessageTimeoutMs(30_000);
        settings.setMaxRetryCount(10);
        settings.setLiveBufferSize(500);
        settings.setReadBatchSize(20);
        settings.setHistoryBufferSize(500);
        settings.setCheckpointAfter(2_000);
        settings.setCheckpointLowerBound(10);
        settings.setCheckpointUpperBound(1_000);
        settings.setMaxSubscriberCount(0);
        settings.setNamedConsumerStrategy(NamedConsumerStrategy.ROUND_ROBIN);
    }

    PersistentSubscriptionSettings(int checkpointAfter, boolean extraStatistics, boolean resolveLinks,
                                          int historyBufferSize, int liveBufferSize, int checkpointUpperBound,
                                          int maxRetryCount, int maxSubscriberCount, int messageTimeoutMs,
                                          int minCheckpointCount, int readBatchSize, NamedConsumerStrategy namedConsumerStrategy) {
        this.checkpointAfter = checkpointAfter;
        this.extraStatistics = extraStatistics;
        this.resolveLinkTos = resolveLinks;
        this.historyBufferSize = historyBufferSize;
        this.liveBufferSize = liveBufferSize;
        this.checkpointUpperBound = checkpointUpperBound;
        this.maxRetryCount = maxRetryCount;
        this.maxSubscriberCount = maxSubscriberCount;
        this.messageTimeoutMs = messageTimeoutMs;
        this.checkpointLowerBound = minCheckpointCount;
        this.readBatchSize = readBatchSize;
        this.namedConsumerStrategy = namedConsumerStrategy;
    }

    /**
     * The amount of time in milliseconds to try to checkpoint after.
     */
    public int getCheckpointAfterInMs() {
        return checkpointAfter;
    }

    /**
     * The amount of time to try to checkpoint after.
     */
    public Duration getCheckpointAfter() {
        return Duration.ofMillis(checkpointAfter);
    }

    /**
     * Whether to track latency statistics on this subscription.
     */
    public boolean isExtraStatistics() {
        return extraStatistics;
    }

    /**
     * If true, link resolution is enabled.
     *
     * The best way to explain link resolution is when using system projections. When reading the stream <i>$streams</i>
     * , each event is actually a link pointing to the first event of a stream. By enabling link resolution feature,
     * KurrentDB will also return the event targeted by the link.
     */
    public boolean shouldResolveLinkTos() {
        return resolveLinkTos;
    }

    /**
     * The number of events to cache when catching up. Default 500.
     */
    public int getHistoryBufferSize() {
        return historyBufferSize;
    }

    /**
     * The size of the buffer (in-memory) listening to live messages as they happen before paging occurs. Default 500.
     */
    public int getLiveBufferSize() {
        return liveBufferSize;
    }

    /**
     * The maximum number of messages not checkpointed before forcing a checkpoint.
     */
    public int getCheckpointUpperBound() {
        return checkpointUpperBound;
    }

    /**
     * The maximum number of retries (due to timeout) before a message is considered to be parked.
     */
    public int getMaxRetryCount() {
        return maxRetryCount;
    }

    /**
     * The maximum number of subscribers allowed.
     */
    public int getMaxSubscriberCount() {
        return maxSubscriberCount;
    }

    /**
     * The amount of time after which to consider a message as timed out and retried.
     */
    public Duration getMessageTimeout() {
        return Duration.ofMillis(messageTimeoutMs);
    }

    /**
     * The amount of time in milliseconds after which to consider a message as timed out and retried.
     */
    public int getMessageTimeoutMs() {
        return messageTimeoutMs;
    }

    /**
     * The minimum number of messages to process before a checkpoint may be written.
     */
    public int getCheckpointLowerBound() {
        return checkpointLowerBound;
    }

    /**
     * The number of events read at a time when catching up.
     */
    public int getReadBatchSize() {
        return readBatchSize;
    }

    /**
     * The strategy to use for distributing events to client consumers.
     */
    public NamedConsumerStrategy getNamedConsumerStrategy() {
        return namedConsumerStrategy;
    }

    /**
     * The amount of time to try checkpoint after in milliseconds.
     */
    void setCheckpointAfter(int checkpointAfter) {
        this.checkpointAfter = checkpointAfter;
    }

    /**
     * Enables tracking of in depth latency statistics on this subscription.
     */
    void setExtraStatistics(boolean extraStatistics) {
        this.extraStatistics = extraStatistics;
    }

    /**
     * If true, link resolution is enabled.
     *
     * The best way to explain link resolution is when using system projections. When reading the stream <i>$streams</i>
     * , each event is actually a link pointing to the first event of a stream. By enabling link resolution feature,
     * KurrentDB will also return the event targeted by the link.
     */
    public boolean isResolveLinkTos() {
        return resolveLinkTos;
    }

    /**
     * Enables link resolution.
     *
     * The best way to explain link resolution is when using system projections. When reading the stream <i>$streams</i>
     * , each event is actually a link pointing to the first event of a stream. By enabling link resolution feature,
     * KurrentDB will also return the event targeted by the link.
     */
    void setResolveLinkTos(boolean resolveLinkTos) {
        this.resolveLinkTos = resolveLinkTos;
    }

    /**
     * The number of events to cache when catching up. Default 500.
     */
    void setHistoryBufferSize(int historyBufferSize) {
        this.historyBufferSize = historyBufferSize;
    }

    /**
     * The size of the buffer (in-memory) listening to live messages as they happen before paging occurs. Default 500.
     */
    void setLiveBufferSize(int liveBufferSize) {
        this.liveBufferSize = liveBufferSize;
    }

    /**
     * The maximum number of messages not checkpointed before forcing a checkpoint.
     */
    void setCheckpointUpperBound(int checkpointUpperBound) {
        this.checkpointUpperBound = checkpointUpperBound;
    }

    /**
     * The maximum number of retries (due to timeout) before a message is considered to be parked.
     */
    void setMaxRetryCount(int maxRetryCount) {
        this.maxRetryCount = maxRetryCount;
    }

    /**
     * The maximum number of subscribers allowed.
     */
    void setMaxSubscriberCount(int maxSubscriberCount) {
        this.maxSubscriberCount = maxSubscriberCount;
    }

    /**
     * The amount of time in milliseconds after which to consider a message as timed out and retried.
     */
    void setMessageTimeoutMs(int messageTimeoutMs) {
        this.messageTimeoutMs = messageTimeoutMs;
    }

    /**
     * The minimum number of messages to process before a checkpoint may be written.
     */
    void setCheckpointLowerBound(int checkpointLowerBound) {
        this.checkpointLowerBound = checkpointLowerBound;
    }

    /**
     * The number of events read at a time when catching up.
     */
    void setReadBatchSize(int readBatchSize) {
        this.readBatchSize = readBatchSize;
    }

    /**
     * The strategy to use for distributing events to client consumers.
     */
    void setNamedConsumerStrategy(NamedConsumerStrategy namedConsumerStrategy) {
        this.namedConsumerStrategy = namedConsumerStrategy;
    }
}
