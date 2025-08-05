package io.kurrent.dbclient;

public class ClientTelemetryConstants {
    public static final String INSTRUMENTATION_NAME = "kurrentdb";

    public static class Metadata {
        public static final String TRACE_ID = "$traceId";
        public static final String SPAN_ID = "$spanId";
    }

    public static class Operations {
        public static final String APPEND = "streams.append";
        public static final String MULTI_STREAM_APPEND = "streams.multi_stream_append";
        public static final String SUBSCRIBE = "streams.subscribe";
    }
}
