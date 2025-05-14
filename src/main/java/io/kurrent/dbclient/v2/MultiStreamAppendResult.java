package io.kurrent.dbclient.v2;

import java.util.Objects;
import java.util.function.Function;

/**
 * Represents the result of appending to multiple streams, which can be either a collection of successes or a collection of failures.
 */
public class MultiStreamAppendResult {
    private final AppendStreamSuccesses successes;
    private final AppendStreamFailures failures;

    private MultiStreamAppendResult(AppendStreamSuccesses successes, AppendStreamFailures failures) {
        this.successes = successes;
        this.failures = failures;
    }

    /**
     * Creates a new MultiStreamAppendResult from a collection of successes.
     *
     * @param successes The collection of success results.
     * @return A new MultiStreamAppendResult.
     */
    public static MultiStreamAppendResult fromSuccesses(AppendStreamSuccesses successes) {
        if (successes == null)
            throw new IllegalArgumentException("Successes cannot be null");

        return new MultiStreamAppendResult(successes, null);
    }

    /**
     * Creates a new MultiStreamAppendResult from a collection of failures.
     *
     * @param failures The collection of failure results.
     * @return A new MultiStreamAppendResult.
     */
    public static MultiStreamAppendResult fromFailures(AppendStreamFailures failures) {
        if (failures == null)
            throw new IllegalArgumentException("Failures cannot be null");

        return new MultiStreamAppendResult(null, failures);
    }

    /**
     * Checks if this result is a collection of successes.
     *
     * @return true if this result is a collection of successes, false otherwise.
     */
    public boolean isSuccesses() {
        return successes != null;
    }

    /**
     * Checks if this result is a collection of failures.
     *
     * @return true if this result is a collection of failures, false otherwise.
     */
    public boolean isFailures() {
        return failures != null;
    }

    /**
     * Gets the collection of success results.
     *
     * @return The collection of success results.
     * @throws IllegalStateException if this result is not a collection of successes.
     */
    public AppendStreamSuccesses getSuccesses() {
        if (!isSuccesses())
            throw new IllegalStateException("Result is not a collection of successes");

        return successes;
    }

    /**
     * Gets the collection of failure results.
     *
     * @return The collection of failure results.
     * @throws IllegalStateException if this result is not a collection of failures.
     */
    public AppendStreamFailures getFailures() {
        if (!isFailures())
            throw new IllegalStateException("Result is not a collection of failures");

        return failures;
    }

    /**
     * Folds the result to the appropriate action.
     *
     * @param successesAction The action to perform if this result is a collection of successes.
     * @param failuresAction The action to perform if this result is a collection of failures.
     * @param <T> The type of the result.
     * @return The result of the action.
     */
    public <T> T fold(Function<AppendStreamSuccesses, T> successesAction,
                      Function<AppendStreamFailures, T> failuresAction) {
        if (isSuccesses())
            return successesAction.apply(successes);
        else
            return failuresAction.apply(failures);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MultiStreamAppendResult that = (MultiStreamAppendResult) o;
        return Objects.equals(successes, that.successes) &&
                Objects.equals(failures, that.failures);
    }

    @Override
    public int hashCode() {
        return Objects.hash(successes, failures);
    }

    @Override
    public String toString() {
        if (isSuccesses()) {
            return "MultiStreamAppendResult{successes=" + successes + '}';
        } else {
            return "MultiStreamAppendResult{failures=" + failures + '}';
        }
    }
}