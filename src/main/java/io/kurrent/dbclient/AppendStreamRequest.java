package io.kurrent.dbclient;

import java.util.Iterator;

public class AppendStreamRequest {
    private final String streamName;
    private final Iterator<EventData> events;
    private final StreamState expectedState;

    public AppendStreamRequest(String streamName, Iterator<EventData> events, StreamState expectedState) {
        this.streamName = streamName;
        this.events = events;
        this.expectedState = expectedState;
    }

    public String getStreamName() {
        return streamName;
    }

    public Iterator<EventData> getEvents() {
        return events;
    }

    public StreamState getExpectedState() {
        return expectedState;
    }
}
