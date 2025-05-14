package io.kurrent.dbclient.v2;

import java.util.Objects;

/**
 * Represents a successful append to a stream.
 */
public class AppendStreamSuccess {
    private final String stream;
    private final long position;
    private final long streamRevision;

    /**
     * Initializes a new instance of the AppendStreamSuccess class.
     *
     * @param stream The stream name.
     * @param position The position in the log.
     * @param streamRevision The stream revision.
     */
    public AppendStreamSuccess(String stream, long position, long streamRevision) {
        this.stream = stream != null ? stream : "";
        this.position = position;
        this.streamRevision = streamRevision;
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
     * Gets the position in the log.
     *
     * @return The position in the log.
     */
    public long getPosition() {
        return position;
    }

    /**
     * Gets the stream revision.
     *
     * @return The stream revision.
     */
    public long getStreamRevision() {
        return streamRevision;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AppendStreamSuccess that = (AppendStreamSuccess) o;
        return position == that.position &&
                streamRevision == that.streamRevision &&
                Objects.equals(stream, that.stream);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stream, position, streamRevision);
    }

    @Override
    public String toString() {
        return "AppendStreamSuccess{" +
                "stream='" + stream + '\'' +
                ", position=" + position +
                ", streamRevision=" + streamRevision +
                '}';
    }
}