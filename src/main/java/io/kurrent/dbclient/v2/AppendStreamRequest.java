package io.kurrent.dbclient.v2;

import io.kurrent.dbclient.StreamState;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a request to append messages to a stream.
 */
public class AppendStreamRequest {
    private final String stream;
    private final List<Message> messages;
    private final StreamState expectedState;
    /**
     * Initializes a new instance of the AppendStreamRequest class.
     *
     * @param stream The stream name.
     * @param messages The messages to append.
     * @param expectedState The expected state of the stream.
     */
    public AppendStreamRequest(@NotNull String stream, @NotNull List<Message> messages, @NotNull StreamState expectedState) {
        this.stream = stream;
        this.messages = messages;
        this.expectedState = expectedState;
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
     * Gets the messages to append.
     *
     * @return The messages to append.
     */
    public List<Message> getMessages() {
        return messages;
    }

    /**
     * Gets the expected state of the stream.
     *
     * @return The expected state of the stream.
     */
    public StreamState getExpectedState() {
        return expectedState;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AppendStreamRequest that = (AppendStreamRequest) o;
        return Objects.equals(stream, that.stream) &&
                Objects.equals(messages, that.messages) &&
                Objects.equals(expectedState, that.expectedState);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stream, messages, expectedState);
    }

    @Override
    public String toString() {
        return "AppendStreamRequest{" +
                "stream='" + stream + '\'' +
                ", messages=" + messages +
                ", expectedState=" + expectedState +
                '}';
    }
}