package io.kurrent.dbclient.v2;

import java.util.Objects;
import java.util.function.Function;

/**
 * Represents the result of appending to a stream, which can be either a success or a failure.
 */
public class AppendStreamResult {
    private final AppendStreamSuccess success;
    private final AppendStreamFailure failure;

    private AppendStreamResult(AppendStreamSuccess success, AppendStreamFailure failure) {
        this.success = success;
        this.failure = failure;
    }

    /**
     * Creates a new AppendStreamResult from a success.
     *
     * @param success The success result.
     * @return A new AppendStreamResult.
     */
    public static AppendStreamResult fromSuccess(AppendStreamSuccess success) {
        if (success == null)
            throw new IllegalArgumentException("Success cannot be null");

        return new AppendStreamResult(success, null);
    }

    /**
     * Creates a new AppendStreamResult from a failure.
     *
     * @param failure The failure result.
     * @return A new AppendStreamResult.
     */
    public static AppendStreamResult fromFailure(AppendStreamFailure failure) {
        if (failure == null)
            throw new IllegalArgumentException("Failure cannot be null");

        return new AppendStreamResult(null, failure);
    }

    /**
     * Checks if this result is a success.
     *
     * @return true if this result is a success, false otherwise.
     */
    public boolean isSuccess() {
        return success != null;
    }

    /**
     * Checks if this result is a failure.
     *
     * @return true if this result is a failure, false otherwise.
     */
    public boolean isFailure() {
        return failure != null;
    }

    /**
     * Gets the success result.
     *
     * @return The success result.
     * @throws IllegalStateException if this result is not a success.
     */
    public AppendStreamSuccess getSuccess() {
        if (!isSuccess())
            throw new IllegalStateException("Result is not a success");

        return success;
    }

    /**
     * Gets the failure result.
     *
     * @return The failure result.
     * @throws IllegalStateException if this result is not a failure.
     */
    public AppendStreamFailure getFailure() {
        if (!isFailure())
            throw new IllegalStateException("Result is not a failure");

        return failure;
    }

    /**
     * Folds the result to the appropriate action.
     *
     * @param successAction The action to perform if this result is a success.
     * @param failureAction The action to perform if this result is a failure.
     * @param <T> The type of the result.
     * @return The result of the action.
     */
    public <T> T fold(Function<AppendStreamSuccess, T> successAction,
                      Function<AppendStreamFailure, T> failureAction) {
        if (isSuccess())
            return successAction.apply(success);
        else
            return failureAction.apply(failure);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AppendStreamResult that = (AppendStreamResult) o;
        return Objects.equals(success, that.success) &&
                Objects.equals(failure, that.failure);
    }

    @Override
    public int hashCode() {
        return Objects.hash(success, failure);
    }

    @Override
    public String toString() {
        if (isSuccess())
            return "AppendStreamResult{success=" + success + '}';
        else
            return "AppendStreamResult{failure=" + failure + '}';
    }
}