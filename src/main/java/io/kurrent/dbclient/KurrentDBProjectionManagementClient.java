package io.kurrent.dbclient;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * Represents KurrentDB client for projections management. A client instance maintains a two-way communication to EventStoreDB.
 * Many threads can use the EventStoreDB client simultaneously, or a single thread can make many asynchronous requests.
 */
public class KurrentDBProjectionManagementClient {
    final KurrentDBClientBase inner;
    private KurrentDBProjectionManagementClient(KurrentDBClientSettings settings) {
        inner = new KurrentDBClientBase(settings);
    }

    private KurrentDBProjectionManagementClient(KurrentDBClientBase inner) {
        this.inner = inner;
    }

    /**
     * Returns the Projection Management client based on the settings.
     * @param settings The settings to use for constructing the client.
     */
    public static KurrentDBProjectionManagementClient create(KurrentDBClientSettings settings) {
        return new KurrentDBProjectionManagementClient(settings);
    }

    /**
     * Returns a Projection Management client based on existing client.
     * @param existingClient Existing client.
     */
    public static KurrentDBProjectionManagementClient from(KurrentDBClientBase existingClient) {
        return new KurrentDBProjectionManagementClient(existingClient);
    }

    /**
     * Stops the projection without writing a checkpoint.
     * This can be used to disable a projection that has been faulted.
     * @param projectionName Name of the projection.
     */
    public CompletableFuture abort(final String projectionName) {
        return this.abort(projectionName, AbortProjectionOptions.get());
    }

    /**
     * Stops the projection without writing a checkpoint.
     * This can be used to disable a projection that has been faulted.
     * @param projectionName Name of the projection.
     * @param options Additional options.
     */
    public CompletableFuture abort(final String projectionName, AbortProjectionOptions options) {
        return new AbortProjection(inner.getGrpcClient(), projectionName, options).execute();
    }

    /**
     * Creates a new projection in the stopped state. Enable needs to be called separately to start the projection.
     * @param projectionName Name of the projection.
     * @param query The JavaScript projection.
     */
    public CompletableFuture create(final String projectionName, final String query) {
        return this.create(projectionName, query, CreateProjectionOptions.get());
    }

    /**
     * Creates a new projection in the stopped state. Enable needs to be called separately to start the projection.
     * @param projectionName Name of the projection.
     * @param query The JavaScript projection.
     * @param options Additional options.
     */
    public CompletableFuture create(final String projectionName, final String query, CreateProjectionOptions options) {
        if (options == null)
            options = CreateProjectionOptions.get();

        return new CreateProjection(inner.getGrpcClient(), projectionName, query, options).execute();
    }

    /**
     * Enables the projection.
     * @param projectionName Name of the projection.
     */
    public CompletableFuture enable(final String projectionName) {
        return this.enable(projectionName, EnableProjectionOptions.get());
    }

    /**
     * Enables the projection.
     * @param projectionName Name of the projection.
     * @param options Additional options.
     */
    public CompletableFuture enable(final String projectionName, EnableProjectionOptions options) {
        return new EnableProjection(inner.getGrpcClient(), projectionName, options).execute();
    }

    /**
     * Deletes the projection.
     * @param projectionName Name of the projection.
     */
    public CompletableFuture delete(final String projectionName) {
        return this.delete(projectionName, DeleteProjectionOptions.get());
    }

    /**
     * Deletes the projection.
     * @param projectionName Name of the projection.
     * @param options Additional options.
     */
    public CompletableFuture delete(final String projectionName, DeleteProjectionOptions options) {
        return new DeleteProjection(inner.getGrpcClient(), projectionName, options).execute();
    }

    /**
     * Disables the projection.
     * @param projectionName Name of the projection.
     */
    public CompletableFuture disable(final String projectionName) {
        return this.disable(projectionName, DisableProjectionOptions.get());
    }

    /**
     * Disables the projection.
     * @param projectionName Name of the projection.
     * @param options Additional options.
     */
    public CompletableFuture disable(final String projectionName, DisableProjectionOptions options) {
        return new DisableProjection(inner.getGrpcClient(), projectionName, options).execute();
    }

    /**
     * Gets the projection's result.
     * @param projectionName Name of the projection.
     * @param type Type of the class to construct for the result.
     * @param <TResult> The result type to return.
     */
    public <TResult> CompletableFuture<TResult>  getResult(final String projectionName, Class<TResult> type) {
        return getResult(projectionName, type, GetProjectionResultOptions.get());
    }

    /**
     * Gets the projection's result.
     * @param projectionName Name of the projection.
     * @param type Type of the class to construct for the result.
     * @param <TResult> The result type to return.
     */
    public <TResult> CompletableFuture<TResult>  getResult(final String projectionName, Class<TResult> type, GetProjectionResultOptions options) {
        return new GetProjectionResult<>(inner.getGrpcClient(), projectionName, options, type).execute();
    }

    /**
     * Gets the projection's result.
     * @param projectionName Name of the projection.
     * @param javaTypeFunction Factory method for constructing the return type.
     * @param <TResult> The result type to return.
     */
    public <TResult> CompletableFuture<TResult> getResult(final String projectionName,
                                                          Function<TypeFactory, JavaType> javaTypeFunction) {
        return getResult(projectionName, javaTypeFunction, GetProjectionResultOptions.get());
    }

    /**
     * Gets the projection's result.
     * @param projectionName Name of the projection.
     * @param javaTypeFunction Factory method for constructing the return type.
     * @param <TResult> The result type to return.
     */
    public <TResult> CompletableFuture<TResult> getResult(final String projectionName,
                                                          Function<TypeFactory, JavaType> javaTypeFunction, GetProjectionResultOptions options) {
        return new GetProjectionResult<TResult>(inner.getGrpcClient(), projectionName, options, javaTypeFunction).execute();
    }

    /**
     * Gets the state of the projection.
     * @param projectionName Name of the projection.
     * @param type Type of the class to construct for the result.
     * @param <TResult> The result type to return.
     */
    public <TResult> CompletableFuture<TResult> getState(final String projectionName, Class<TResult> type) {
        return getState(projectionName, type, GetProjectionStateOptions.get());
    }

    /**
     * Gets the state of the projection.
     * @param projectionName Name of the projection.
     * @param type Type of the class to construct for the result.
     * @param <TResult> The result type to return.
     */
    public <TResult> CompletableFuture<TResult> getState(final String projectionName, Class<TResult> type, GetProjectionStateOptions options) {
        return new GetProjectionState<>(inner.getGrpcClient(), projectionName, options, type).execute();
    }

    /**
     * Gets the state of the projection.
     * @param projectionName Name of the projection.
     * @param javaTypeFunction Factory method for constructing the return type.
     * @param <TResult> The result type to return.
     */
    public <TResult> CompletableFuture<TResult> getState(final String projectionName,
                                                         Function<TypeFactory, JavaType> javaTypeFunction) {
        return getState(projectionName, javaTypeFunction, GetProjectionStateOptions.get());
    }

    /**
     * Gets the state of the projection.
     * @param projectionName Name of the projection.
     * @param javaTypeFunction Factory method for constructing the return type.
     * @param <TResult> The result type to return.
     */
    public <TResult> CompletableFuture<TResult> getState(final String projectionName,
                                                         Function<TypeFactory, JavaType> javaTypeFunction, GetProjectionStateOptions options) {
        return new GetProjectionState<TResult>(inner.getGrpcClient(), projectionName, options, javaTypeFunction).execute();
    }

    /**
     * Gets the statistics for the projection.
     * @param projectionName Name of the projection.
     */
    public CompletableFuture<ProjectionDetails> getStatistics(final String projectionName) {
        return this.getStatistics(projectionName, GetProjectionStatisticsOptions.get());
    }

    /**
     * Gets the statistics for the projection.
     * @param projectionName Name of the projection.
     * @param options Additional options.
     */
    public CompletableFuture<ProjectionDetails> getStatistics(final String projectionName, GetProjectionStatisticsOptions options) {
        return new GetProjectionStatistics(inner.getGrpcClient(), projectionName, options).execute();
    }

    /**
     * Gets the projection's current status.
     * @param projectionName Name of the projection.
     */
    public CompletableFuture<ProjectionDetails> getStatus(final String projectionName) {
        return this.getStatus(projectionName, GetProjectionStatusOptions.get());
    }

    /**
     * Gets the projection's current status.
     * @param projectionName Name of the projection.
     * @param options Additional options.
     */
    public CompletableFuture<ProjectionDetails> getStatus(final String projectionName, final GetProjectionStatusOptions options) {
        return new GetProjectionStatus(inner.getGrpcClient(), projectionName, options).execute();
    }

    /**
     * Lists all continuous projections.
     */
    public CompletableFuture<List<ProjectionDetails>> list() {
        return list(ListProjectionsOptions.get());
    }

    /**
     * Lists all continuous projections.
     * @param options Additional options.
     */
    public CompletableFuture<List<ProjectionDetails>> list(ListProjectionsOptions options) {
        return new ListProjections(inner.getGrpcClient(), options).execute().thenApply(ListProjectionsResult::getProjections);
    }

    /**
     * Resets the projection, causing it to start again from the beginning of the stream/s it selects from.
     * Resetting a projection will truncate all emitted streams and re-emit all events.
     * @param projectionName Name of the projection.
     */
    public CompletableFuture reset(final String projectionName) {
        return this.reset(projectionName, ResetProjectionOptions.get());
    }

    /**
     * Resets the projection, causing it to start again from the beginning of the stream/s it selects from.
     * Resetting a projection will truncate all emitted streams and re-emit all events.
     * @param projectionName Name of the projection.
     * @param options Additional options.
     */
    public CompletableFuture reset(final String projectionName, ResetProjectionOptions options) {
        return new ResetProjection(inner.getGrpcClient(), projectionName, options).execute();
    }

    /**
     * Restarts the projection subsystem. This can be used to recover from certain kinds of errors.
     */
    public CompletableFuture restartSubsystem() {
        return this.restartSubsystem(RestartProjectionSubsystemOptions.get());
    }

    /**
     * Restarts the projection subsystem. This can be used to recover from certain kinds of errors.
     * @param options Additional options.
     */
    public CompletableFuture restartSubsystem(RestartProjectionSubsystemOptions options) {
        return new RestartProjectionSubsystem(inner.getGrpcClient(), options).execute();
    }

    /**
     * Updates the projection's query and emit options.
     * @param projectionName Name of the projection.
     * @param query The JavaScript projection.
     */
    public CompletableFuture update(final String projectionName, final String query) {
        return this.update(projectionName, query, UpdateProjectionOptions.get());
    }

    /**
     * Updates the projection's query and emit options.
     * @param projectionName Name of the projection.
     * @param query The JavaScript projection.
     * @param options Additional options.
     */
    public CompletableFuture update(final String projectionName, final String query, UpdateProjectionOptions options) {
        return new UpdateProjection(inner.getGrpcClient(), projectionName, query, options).execute();
    }

    /**
     * Closes a connection and cleans all its allocated resources.
     */
    public CompletableFuture<Void> shutdown() {
        return inner.shutdown();
    }

    /**
     * Checks if this client instance has been shutdown.
     * After shutdown a client instance can no longer process new operations and
     * a new client instance has to be created.
     * @return {@code true} if client instance has been shutdown.
     */
    public boolean isShutdown() {
        return inner.isShutdown();
    }
}
