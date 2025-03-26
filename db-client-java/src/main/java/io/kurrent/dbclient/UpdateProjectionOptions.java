package io.kurrent.dbclient;

/**
 * Options of the update projection request.
 */
public class UpdateProjectionOptions extends OptionsBase<UpdateProjectionOptions> {
    private boolean emitEnabled;

    private UpdateProjectionOptions() {
    }

    /**
     * Returns options with default values.
     */
    public static UpdateProjectionOptions get() {
        return new UpdateProjectionOptions();
    }

    /**
     * Allows the projection to write events.
     */
    public UpdateProjectionOptions emitEnabled(boolean value) {
        this.emitEnabled = value;
        return this;
    }

    boolean isEmitEnabled() {
        return emitEnabled;
    }
}
