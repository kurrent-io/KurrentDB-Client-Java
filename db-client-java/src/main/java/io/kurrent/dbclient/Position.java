package io.kurrent.dbclient;

import javax.validation.constraints.NotNull;

import java.util.Objects;

/**
 * Transaction log position.
 */
public class Position implements Comparable<Position> {
    private final long prepare;
    private final long commit;

    public Position(long commitUnsigned, long prepareUnsigned) {
        // Do not allow the commit position to be less than the prepare position
        if (Long.compareUnsigned(commitUnsigned, prepareUnsigned) < 0) {
            throw new IllegalArgumentException("The commit position may not be before the prepare position");
        }

        this.prepare = prepareUnsigned;
        this.commit = commitUnsigned;
    }

    public Position(String prepare, String commit) {
        this(Long.parseUnsignedLong(commit), Long.parseUnsignedLong(prepare));
    }

    /**
     * Returns the prepare position.
     */
    public long getPrepareUnsigned() {
        return prepare;
    }

    /**
     * Returns the commit position.
     */
    public long getCommitUnsigned() {
        return commit;
    }

    @Override
    public String toString() {
        return String.format("%s/%s", Long.toUnsignedString(commit), Long.toUnsignedString(prepare));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Position position = (Position) o;
        return prepare == position.prepare &&
                commit == position.commit;
    }

    @Override
    public int hashCode() {
        return Objects.hash(prepare, commit);
    }

    @Override
    public int compareTo(@NotNull Position other) {
        if (this.commit == other.commit && this.prepare == other.prepare) {
            return 0;
        }

        if ((Long.compareUnsigned(this.commit, other.commit) < 0) || (Long.compareUnsigned(this.commit, other.commit) == 0 && Long.compareUnsigned(this.prepare, other.prepare) < 0)) {
            return -1;
        }

        return 1;
    }
}
