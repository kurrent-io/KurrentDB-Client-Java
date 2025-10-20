package io.kurrent.dbclient;

import java.util.List;

public class MultiStreamAppendResponse {
    private final long position;
    private final List<AppendResponse> results;

    public MultiStreamAppendResponse(long position, List<AppendResponse> results) {
        this.position = position;
        this.results = results;
    }

    public long getPosition() {
        return position;
    }

    public List<AppendResponse> getResults() {
        return results;
    }
}
