package io.kurrent.dbclient.v2;

import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * Represents a failed append to a stream.
 */
public class AppendStreamFailure {
    private final String stream;
    private final Exception error;

    /**
     * Initializes a new instance of the AppendStreamFailure class.
     *
     * @param stream The stream name.
     * @param error The error that occurred.
     */
    public AppendStreamFailure(@NotNull String stream, Exception error) {
        this.stream = stream;
        this.error = error;
    }

    /**
     * Gets the stream name.
     *
     * @return The stream name.
     */
    public String getStream() {
        return stream;
    }

    /**
     * Gets the error that occurred.
     *
     * @return The error that occurred.
     */
    public Exception getError() {
        return error;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AppendStreamFailure that = (AppendStreamFailure) o;
        return Objects.equals(stream, that.stream) &&
                Objects.equals(error, that.error);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stream, error);
    }

    @Override
    public String toString() {
        return "AppendStreamFailure{" +
                "stream='" + stream + '\'' +
                ", error=" + error +
                '}';
    }
}