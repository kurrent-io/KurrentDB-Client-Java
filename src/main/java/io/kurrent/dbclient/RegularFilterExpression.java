package io.kurrent.dbclient;

import javax.validation.constraints.NotNull;
import java.util.Objects;
import java.util.regex.Pattern;

class RegularFilterExpression implements Comparable<RegularFilterExpression> {
    @NotNull
    private final Pattern value;

    public RegularFilterExpression(@NotNull Pattern value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value.pattern();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RegularFilterExpression that = (RegularFilterExpression) o;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public int compareTo(RegularFilterExpression other) {
        String ours = this.value.pattern();
        String theirs = other.value.pattern();

        return ours.compareTo(theirs);
    }
}
