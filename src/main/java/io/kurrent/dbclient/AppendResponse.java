package io.kurrent.dbclient;

public class AppendResponse {
    private final String stream;
    private final long streamRevision;

    public AppendResponse(String stream, long streamRevision) {
        this.stream = stream;
        this.streamRevision = streamRevision;
    }

    public String getStream() {
        return stream;
    }

    public long getStreamRevision() {
        return streamRevision;
    }
}
