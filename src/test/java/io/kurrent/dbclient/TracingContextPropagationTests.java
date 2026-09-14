package io.kurrent.dbclient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

public class TracingContextPropagationTests {
    private static final String TRACE_ID = "0af7651916cd43dd8448eb211c80319c";
    private static final String SPAN_ID = "b7ad6b7169203331";
    private static final String STALE_METADATA = "{"
            + "\"$traceparent\":\"00-11111111111111111111111111111111-1111111111111111-01\","
            + "\"$tracestate\":\"dd=s:1\","
            + "\"$traceId\":\"11111111111111111111111111111111\","
            + "\"$spanId\":\"1111111111111111\""
            + "}";

    private static Span spanWith(TraceFlags flags, TraceState traceState) {
        return Span.wrap(SpanContext.create(TRACE_ID, SPAN_ID, flags, traceState));
    }

    private static ObjectNode parseMetadata(byte[] metadata) throws Exception {
        return new ObjectMapper().readValue(metadata, ObjectNode.class);
    }

    @Test
    public void testInjectsSampledTraceContextAlongsideLegacyFields() throws Exception {
        TraceState traceState = TraceState.builder().put("dd", "s:1").build();
        Span span = spanWith(TraceFlags.getSampled(), traceState);
        byte[] userMetadata = "{\"foo\":\"bar\"}".getBytes(StandardCharsets.UTF_8);

        ObjectNode metadata = parseMetadata(ClientTelemetry.tryInjectTracingContext(span, userMetadata));

        Assertions.assertEquals(
                "00-" + TRACE_ID + "-" + SPAN_ID + "-01",
                metadata.get(ClientTelemetryConstants.Metadata.TRACE_PARENT).asText());
        Assertions.assertEquals("dd=s:1", metadata.get(ClientTelemetryConstants.Metadata.TRACE_STATE).asText());
        Assertions.assertEquals(TRACE_ID, metadata.get(ClientTelemetryConstants.Metadata.TRACE_ID).asText());
        Assertions.assertEquals(SPAN_ID, metadata.get(ClientTelemetryConstants.Metadata.SPAN_ID).asText());
        Assertions.assertEquals("bar", metadata.get("foo").asText());
    }

    @Test
    public void testInjectsUnsampledTraceContextAndStripsStaleTracingFields() throws Exception {
        Span span = spanWith(TraceFlags.getDefault(), TraceState.getDefault());

        ObjectNode metadata = parseMetadata(ClientTelemetry.tryInjectTracingContext(
                span, STALE_METADATA.getBytes(StandardCharsets.UTF_8)));

        Assertions.assertEquals(
                "00-" + TRACE_ID + "-" + SPAN_ID + "-00",
                metadata.get(ClientTelemetryConstants.Metadata.TRACE_PARENT).asText());
        Assertions.assertNull(metadata.get(ClientTelemetryConstants.Metadata.TRACE_ID));
        Assertions.assertNull(metadata.get(ClientTelemetryConstants.Metadata.SPAN_ID));
        Assertions.assertNull(metadata.get(ClientTelemetryConstants.Metadata.TRACE_STATE));
    }

    @Test
    public void testSkipsInjectionForInvalidSpanOrNonJsonObjectMetadata() {
        List<EventData> events = Collections.singletonList(
                EventData.builderAsJson("TestEvent", "{}".getBytes(StandardCharsets.UTF_8)).build());
        byte[] jsonMetadata = "{\"foo\":\"bar\"}".getBytes(StandardCharsets.UTF_8);
        byte[] nonJsonMetadata = "clearlynotvalidjson".getBytes(StandardCharsets.UTF_8);
        Span validSpan = spanWith(TraceFlags.getSampled(), TraceState.getDefault());

        Assertions.assertSame(events, ClientTelemetry.tryInjectTracingContext(Span.getInvalid(), events));
        Assertions.assertSame(jsonMetadata, ClientTelemetry.tryInjectTracingContext(Span.getInvalid(), jsonMetadata));
        Assertions.assertArrayEquals(nonJsonMetadata, ClientTelemetry.tryInjectTracingContext(validSpan, nonJsonMetadata));
    }

    @Test
    public void testExtractionPrefersTraceParentAndPreservesFlagsAndTraceState() {
        String metadata = "{"
                + "\"$traceparent\":\"00-" + TRACE_ID + "-" + SPAN_ID + "-00\","
                + "\"$tracestate\":\"dd=s:1\","
                + "\"$traceId\":\"11111111111111111111111111111111\","
                + "\"$spanId\":\"1111111111111111\""
                + "}";

        SpanContext extracted = ClientTelemetry.tryExtractTracingContext(metadata.getBytes(StandardCharsets.UTF_8));

        Assertions.assertNotNull(extracted);
        Assertions.assertEquals(TRACE_ID, extracted.getTraceId());
        Assertions.assertEquals(SPAN_ID, extracted.getSpanId());
        Assertions.assertFalse(extracted.isSampled());
        Assertions.assertTrue(extracted.isRemote());
        Assertions.assertEquals("s:1", extracted.getTraceState().get("dd"));
    }

    @Test
    public void testExtractionFallsBackToLegacyFieldsAsSampled() {
        String legacyOnly = "{\"$traceId\":\"" + TRACE_ID + "\",\"$spanId\":\"" + SPAN_ID + "\"}";
        String malformedTraceParent = "{"
                + "\"$traceparent\":\"not-a-traceparent\","
                + "\"$traceId\":\"" + TRACE_ID + "\","
                + "\"$spanId\":\"" + SPAN_ID + "\""
                + "}";

        for (String metadata : new String[]{legacyOnly, malformedTraceParent}) {
            SpanContext extracted = ClientTelemetry.tryExtractTracingContext(metadata.getBytes(StandardCharsets.UTF_8));

            Assertions.assertNotNull(extracted);
            Assertions.assertEquals(TRACE_ID, extracted.getTraceId());
            Assertions.assertEquals(SPAN_ID, extracted.getSpanId());
            Assertions.assertTrue(extracted.isSampled());
            Assertions.assertTrue(extracted.isRemote());
        }
    }

    @Test
    public void testExtractionReturnsNullWhenNoTracingMetadataIsPresent() {
        Assertions.assertNull(ClientTelemetry.tryExtractTracingContext(null));
        Assertions.assertNull(ClientTelemetry.tryExtractTracingContext(
                "{\"foo\":\"bar\"}".getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    public void testRoundTripPreservesSamplingDecisionAndTraceState() {
        TraceState traceState = TraceState.builder().put("dd", "s:0").build();
        Span span = spanWith(TraceFlags.getDefault(), traceState);

        byte[] metadata = ClientTelemetry.tryInjectTracingContext(span, (byte[]) null);
        SpanContext extracted = ClientTelemetry.tryExtractTracingContext(metadata);

        Assertions.assertNotNull(extracted);
        Assertions.assertEquals(TRACE_ID, extracted.getTraceId());
        Assertions.assertEquals(SPAN_ID, extracted.getSpanId());
        Assertions.assertFalse(extracted.isSampled());
        Assertions.assertEquals("s:0", extracted.getTraceState().get("dd"));
    }
}
