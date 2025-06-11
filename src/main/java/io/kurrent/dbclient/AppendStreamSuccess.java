package io.kurrent.dbclient;

public class AppendStreamSuccess {
    private final io.kurrentdb.v2.AppendStreamSuccess inner;

    AppendStreamSuccess(io.kurrentdb.v2.AppendStreamSuccess inner) {
        this.inner = inner;
    }

    public String getStreamName() {
        return this.inner.getStream();
    }

    public long getStreamRevision() {
        return this.inner.getStreamRevision();
    }

    public long getPosition() {
        return this.inner.getPosition();
    }
}
