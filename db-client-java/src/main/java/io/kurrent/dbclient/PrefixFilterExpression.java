package io.kurrent.dbclient;

import javax.validation.constraints.NotNull;
import java.util.Objects;

class PrefixFilterExpression implements Comparable<PrefixFilterExpression> {
    @NotNull
    private final String value;
    public PrefixFilterExpression(@NotNull String value) {
        this.value = value;
    }
    @Override
    public String toString() {
        return this.value;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PrefixFilterExpression that = (PrefixFilterExpression) o;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public int compareTo(PrefixFilterExpression other) {
        return this.value.compareTo(other.value);
    }
}
