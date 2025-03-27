package io.kurrent.dbclient;

/**
 * Options for create projection request.
 */
public class CreateProjectionOptions extends OptionsBase<CreateProjectionOptions> {
    private boolean trackEmittedStreams;
    private boolean emitEnabled;

    private CreateProjectionOptions() {
        this.trackEmittedStreams = false;
    }

    /**
     * Returns options with default values.
     */
    public static CreateProjectionOptions get() {
        return new CreateProjectionOptions();
    }

    boolean isTrackingEmittedStreams() {
        return trackEmittedStreams;
    }

    boolean isEmitEnabled() {
        return emitEnabled;
    }

    /**
     * If true, the projection tracks all streams it creates.
     */
    public CreateProjectionOptions trackEmittedStreams(boolean trackEmittedStreams) {
        this.trackEmittedStreams = trackEmittedStreams;
        return this;
    }

    /**
     * If true, allows the projection to emit events.
     */
    public CreateProjectionOptions emitEnabled(boolean value) {
        this.emitEnabled = value;
        return this;
    }
}
