package io.kurrent.dbclient;

/**
 * Options of the replay parked messages request.
 */
public class ReplayParkedMessagesOptions extends OptionsBase<ReplayParkedMessagesOptions> {
    Long stopAt = null;

    ReplayParkedMessagesOptions(){}

    Long getStopAt() {
        return stopAt;
    }

    /**
     * Replay the parked messages until the event revision within the parked messages stream is reached.
     */
    public ReplayParkedMessagesOptions stopAt(long value) {
        this.stopAt = value;
        return this;
    }

    /**
     * Options with default values.
     */
    public static ReplayParkedMessagesOptions get() {
        return new ReplayParkedMessagesOptions();
    }
}
