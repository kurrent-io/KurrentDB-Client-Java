package io.kurrent.dbclient.telemetry;

import io.kurrent.dbclient.ClientTelemetryAttributes;
import io.kurrent.dbclient.ClientTelemetryConstants;
import io.kurrent.dbclient.ConnectionAware;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.internal.data.ExceptionEventData;
import org.junit.jupiter.api.Assertions;

import java.util.List;
import java.util.function.Consumer;

public interface TelemetryAware extends ConnectionAware {
    void onOperationSpanEnded(String operation, Consumer<ReadableSpan> onSpanEnded);
    List<ReadableSpan> getSpansForOperation(String operation);

    default void assertAppendSpanHasExpectedAttributes(ReadableSpan span, String streamName) {
        assertSpanAttributeEquals(span, ClientTelemetryAttributes.Database.SYSTEM, ClientTelemetryConstants.INSTRUMENTATION_NAME);
        assertSpanAttributeEquals(span, ClientTelemetryAttributes.Database.OPERATION, ClientTelemetryConstants.Operations.APPEND);
        assertSpanAttributeEquals(span, ClientTelemetryAttributes.EventStore.STREAM, streamName);
        assertSpanAttributeEquals(span, ClientTelemetryAttributes.Database.USER, "admin");
        Assertions.assertEquals(StatusCode.OK, span.toSpanData().getStatus().getStatusCode());
        Assertions.assertEquals(SpanKind.CLIENT, span.getKind());
    }

    default void assertErroneousSpanHasExpectedAttributes(ReadableSpan span, Throwable actualException) {
        SpanData spanData = span.toSpanData();
        List<io.opentelemetry.sdk.trace.data.EventData> spanEvents = spanData.getEvents();

        Assertions.assertEquals(StatusCode.ERROR, spanData.getStatus().getStatusCode());
        Assertions.assertEquals(1, spanEvents.size());

        ExceptionEventData errorEvent = (ExceptionEventData) spanEvents.get(0);
        Assertions.assertEquals(errorEvent.getException(), actualException);
    }

    default void assertSubscriptionActivityHasExpectedAttributes(ReadableSpan span, String streamName, String subscriptionId, String eventId, String eventType) {
        assertSpanAttributeEquals(span, ClientTelemetryAttributes.Database.SYSTEM, ClientTelemetryConstants.INSTRUMENTATION_NAME);
        assertSpanAttributeEquals(span, ClientTelemetryAttributes.Database.OPERATION, ClientTelemetryConstants.Operations.SUBSCRIBE);
        assertSpanAttributeEquals(span, ClientTelemetryAttributes.Database.USER, "admin");
        assertSpanAttributeEquals(span, ClientTelemetryAttributes.EventStore.STREAM, streamName);
        assertSpanAttributeEquals(span, ClientTelemetryAttributes.EventStore.SUBSCRIPTION_ID, subscriptionId);
        assertSpanAttributeEquals(span, ClientTelemetryAttributes.EventStore.EVENT_ID, eventId);
        assertSpanAttributeEquals(span, ClientTelemetryAttributes.EventStore.EVENT_TYPE, eventType);
        Assertions.assertEquals(StatusCode.OK, span.toSpanData().getStatus().getStatusCode());
        Assertions.assertEquals(SpanKind.CONSUMER, span.getKind());
    }

    default void assertSpanAttributeEquals(ReadableSpan span, String attributeKey, String expectedValue) {
        Assertions.assertEquals(expectedValue, span.getAttribute(AttributeKey.stringKey(attributeKey)));
    }
}
