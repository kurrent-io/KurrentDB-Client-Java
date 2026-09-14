package io.kurrent.dbclient;

public class ClientTelemetryConstants {
    public static final String INSTRUMENTATION_NAME = "kurrentdb";

    public static class Metadata {
        public static final String TRACE_ID = "$traceId";
        public static final String SPAN_ID = "$spanId";
        public static final String TRACE_PARENT = "$traceparent";
        public static final String TRACE_STATE = "$tracestate";
    }

    public static class Operations {
        public static final String APPEND = "streams.append";
        public static final String MULTI_APPEND = "streams.multi-append";
        public static final String SUBSCRIBE = "streams.subscribe";
    }
}
