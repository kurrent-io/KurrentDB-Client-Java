package io.kurrent.dbclient;

import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * Object returned on a successful stream deletion request.
 */
public class DeleteResult {
    private final Position logPosition;

    DeleteResult(@NotNull Position logPosition) {
        this.logPosition = logPosition;
    }

    /**
     * Returns the transaction log position of the stream deletion.
     */
    public Position getPosition() {
        return logPosition;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeleteResult that = (DeleteResult) o;
        return logPosition.equals(that.logPosition);
    }

    @Override
    public int hashCode() {
        return Objects.hash(logPosition);
    }

    @Override
    public String toString() {
        return "DeleteResult{" +
                "logPosition=" + logPosition +
                '}';
    }
}
