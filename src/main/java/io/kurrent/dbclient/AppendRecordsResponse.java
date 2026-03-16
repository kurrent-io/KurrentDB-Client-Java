package io.kurrent.dbclient;

import java.util.List;

public class AppendRecordsResponse {
    private final long position;
    private final List<AppendResponse> results;

    public AppendRecordsResponse(long position, List<AppendResponse> results) {
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
