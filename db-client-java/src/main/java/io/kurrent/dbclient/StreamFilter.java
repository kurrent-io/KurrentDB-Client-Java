package io.kurrent.dbclient;

import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

class StreamFilter implements EventFilter {
    private final PrefixFilterExpression[] prefixFilterExpressions;
    private final RegularFilterExpression regularFilterExpression;
    @NotNull
    private final Optional<Integer> maxSearchWindow;

    public StreamFilter(@NotNull Optional<Integer> maxSearchWindow, RegularFilterExpression regex) {
        this.maxSearchWindow = maxSearchWindow;
        this.regularFilterExpression = regex;
        this.prefixFilterExpressions = null;
    }

    public StreamFilter(@NotNull Optional<Integer> maxSearchWindow, PrefixFilterExpression... prefixes) {
        this.maxSearchWindow = maxSearchWindow;
        this.prefixFilterExpressions = prefixes;
        this.regularFilterExpression = null;
    }

    @Override
    public PrefixFilterExpression[] getPrefixFilterExpressions() {
        return this.prefixFilterExpressions;
    }

    @Override
    public RegularFilterExpression getRegularFilterExpression() {
        return regularFilterExpression;
    }

    @Override
    public Optional<Integer> getMaxSearchWindow() {
        return maxSearchWindow;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StreamFilter that = (StreamFilter) o;
        return Arrays.equals(prefixFilterExpressions, that.prefixFilterExpressions) &&
                Objects.equals(regularFilterExpression, that.regularFilterExpression) &&
                maxSearchWindow.equals(that.maxSearchWindow);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(regularFilterExpression, maxSearchWindow);
        result = 31 * result + Arrays.hashCode(prefixFilterExpressions);
        return result;
    }
}
