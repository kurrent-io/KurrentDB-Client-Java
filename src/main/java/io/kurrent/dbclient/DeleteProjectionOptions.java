package io.kurrent.dbclient;

/**
 * Options of the delete projection request.
 */
public class DeleteProjectionOptions extends OptionsBase<DeleteProjectionOptions> {
    private boolean deleteEmittedStreams;
    private boolean deleteStateStream;
    private boolean deleteCheckpointStream;

    private DeleteProjectionOptions() {
    }

    /**
     * Returns options with default values.
     */
    public static DeleteProjectionOptions get() {
        return new DeleteProjectionOptions();
    }

    /**
     * Deletes emitted streams if the projections had track emitted streams enabled.
     */
    public DeleteProjectionOptions deleteEmittedStreams() {
        deleteEmittedStreams = true;
        return this;
    }

    /**
     * Deletes the projection state stream.
     */
    public DeleteProjectionOptions deleteStateStream() {
        deleteStateStream = true;
        return this;
    }

    /**
     * Deletes the projection checkpoint stream.
     */
    public DeleteProjectionOptions deleteCheckpointStream() {
        deleteCheckpointStream = true;
        return this;
    }

    /**
     * If true, deletes emitted streams if the projections had track emitted streams enabled.
     */
    public DeleteProjectionOptions deleteEmittedStreams(boolean delete) {
        deleteEmittedStreams = delete;
        return this;
    }

    /**
     * If true, deletes the projection state stream.
     */
    public DeleteProjectionOptions deleteStateStream(boolean delete) {
        deleteStateStream = delete;
        return this;
    }

    /**
     * If true, deletes the projection checkpoint stream.
     */
    public DeleteProjectionOptions deleteCheckpointStream(boolean delete) {
        deleteCheckpointStream = delete;
        return this;
    }

    boolean getDeleteEmittedStreams() {
        return deleteEmittedStreams;
    }

    boolean getDeleteStateStream() {
        return deleteStateStream;
    }

    boolean getDeleteCheckpointStream() {
        return deleteCheckpointStream;
    }
}
