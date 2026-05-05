package io.kurrent.dbclient;

/**
 * Options for create projection request.
 */
public class CreateProjectionOptions extends OptionsBase<CreateProjectionOptions> {
    private boolean trackEmittedStreams;
    private boolean emitEnabled;
    private int engineVersion;

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

    int getEngineVersion() {
        return engineVersion;
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

    /**
     * Selects the projection engine version. {@code 0} (default) or {@code 1} selects V1;
     * {@code 2} selects the V2 engine, which provides partition-based parallel processing.
     * <p>
     * The engine version is pinned at create time and cannot be changed via update.
     * V2 has limitations versus V1: {@code trackEmittedStreams} is rejected,
     * result streams are not emitted, and bi-state projections are not supported.
     */
    public CreateProjectionOptions engineVersion(int value) {
        this.engineVersion = value;
        return this;
    }
}
