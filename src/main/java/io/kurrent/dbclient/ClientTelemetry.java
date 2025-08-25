package io.kurrent.dbclient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.grpc.ManagedChannel;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.*;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.BiFunction;

import static io.kurrentdb.protocol.streams.v2.AppendStreamFailure.*;

class ClientTelemetry {
    private static final ClientTelemetryTags DEFAULT_ATTRIBUTES = new ClientTelemetryTags() {{
        put(ClientTelemetryAttributes.Database.SYSTEM, ClientTelemetryConstants.INSTRUMENTATION_NAME);
    }};

    private static Tracer getTracer() {
        return GlobalOpenTelemetry.getTracer(
                ClientTelemetry.class.getPackage().getName(),
                ClientTelemetry.class.getPackage().getImplementationVersion());
    }

    private static List<EventData> tryInjectTracingContext(Span span, List<EventData> events) {
        if (!span.getSpanContext().isValid() || !span.getSpanContext().isSampled())
            return events;

        List<EventData> injectedEvents = new ArrayList<>();
        for (EventData event : events) {
            boolean isJsonEvent = Objects.equals(event.getContentType(), ContentType.JSON);

            injectedEvents.add(EventDataBuilder
                    .binary(event.getEventId(), event.getEventType(), event.getEventData(), isJsonEvent)
                    .metadataAsBytes(tryInjectTracingContext(span, event.getUserMetadata()))
                    .build());
        }
        return injectedEvents;
    }

    private static byte[] tryInjectTracingContext(Span span, byte[] userMetadataBytes) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode userMetadata = userMetadataBytes != null
                    ? objectMapper.readValue(userMetadataBytes, ObjectNode.class)
                    : objectMapper.createObjectNode();

            userMetadata.put(ClientTelemetryConstants.Metadata.TRACE_ID, span.getSpanContext().getTraceId());
            userMetadata.put(ClientTelemetryConstants.Metadata.SPAN_ID, span.getSpanContext().getSpanId());

            return objectMapper.writeValueAsBytes(userMetadata);
        } catch (Throwable t) {
            // User metadata may not be a valid JSON object, or not JSON altogether.
            return userMetadataBytes;
        }
    }

    private static SpanContext tryExtractTracingContext(byte[] userMetadataBytes) {
        if (userMetadataBytes == null)
            return null;

        try {
            ObjectNode userMetadata = new ObjectMapper().readValue(userMetadataBytes, ObjectNode.class);

            JsonNode traceIdNode = userMetadata.get(ClientTelemetryConstants.Metadata.TRACE_ID);
            JsonNode spanIdNode = userMetadata.get(ClientTelemetryConstants.Metadata.SPAN_ID);

            if (traceIdNode == null || spanIdNode == null)
                return null;

            String traceId = traceIdNode.asText();
            String spanId = spanIdNode.asText();

            if (!TraceId.isValid(traceId) || !SpanId.isValid(spanId))
                return null;

            return SpanContext.createFromRemoteParent(traceId, spanId, TraceFlags.getSampled(),
                    TraceState.getDefault());
        } catch (Throwable t) {
            return null;
        }
    }

    static CompletableFuture<WriteResult> traceAppend(
            BiFunction<ManagedChannel, List<EventData>, CompletableFuture<WriteResult>> appendOperation,
            ManagedChannel channel,
            List<EventData> events, String streamId, KurrentDBClientSettings settings,
            UserCredentials optionalCallCredentials) {
        Span span = createSpan(
                ClientTelemetryConstants.Operations.APPEND,
                SpanKind.CLIENT,
                null,
                ClientTelemetryTags.builder()
                        .withRequiredTag(ClientTelemetryAttributes.KurrentDB.STREAM, streamId)
                        .withServerTagsFromGrpcChannel(channel)
                        .withServerTagsFromClientSettings(settings)
                        .withOptionalDatabaseUserTag(settings.getDefaultCredentials())
                        .withOptionalDatabaseUserTag(optionalCallCredentials)
                        .build());

        try (Scope scope = span.makeCurrent()) {
            return appendOperation
                    .apply(channel, tryInjectTracingContext(span, events))
                    .handle(((writeResult, throwable) -> {
                        if (throwable != null) {
                            span.setStatus(StatusCode.ERROR);
                            span.recordException(throwable);
                            span.end();
                            throw new CompletionException(throwable);
                        } else {
                            span.setStatus(StatusCode.OK);
                            span.end();
                            return writeResult;
                        }
                    }));
        }
    }

    static CompletableFuture<MultiAppendWriteResult> traceMultiStreamAppend(
            BiFunction<WorkItemArgs, Iterator<AppendStreamRequest>, CompletableFuture<MultiAppendWriteResult>> multiAppendOperation,
            WorkItemArgs args,
            Iterator<AppendStreamRequest> requests, KurrentDBClientSettings settings) {

        List<AppendStreamRequest> requestsWithTracing = new ArrayList<>();

        Span span = createSpan(
                ClientTelemetryConstants.Operations.MULTI_APPEND,
                SpanKind.CLIENT,
                null,
                ClientTelemetryTags.builder()
                        .withServerTagsFromGrpcChannel(args.getChannel())
                        .withServerTagsFromClientSettings(settings)
                        .withOptionalDatabaseUserTag(settings.getDefaultCredentials())
                        .build());

        while (requests.hasNext()) {
            AppendStreamRequest request = requests.next();

            List<EventData> eventsWithTracing = new ArrayList<>();
            while (request.getEvents().hasNext())
                eventsWithTracing.add(request.getEvents().next());

            List<EventData> tracedEvents = tryInjectTracingContext(span, eventsWithTracing);

            requestsWithTracing.add(new AppendStreamRequest(
                    request.getStreamName(),
                    tracedEvents.iterator(),
                    request.getExpectedState()
            ));
        }

        return multiAppendOperation.apply(args, requestsWithTracing.iterator())
                .handle((result, throwable) -> {
                    if (throwable != null) {
                        span.setStatus(StatusCode.ERROR);
                        span.recordException(throwable);
                        span.end();
                        throw new CompletionException(throwable);
                    } else {
                        if (result.getFailures().isPresent()) {
                            for (AppendStreamFailure failure : result.getFailures().get()) {
                                failure.visit(new MultiAppendStreamErrorVisitor() {
                                    @Override
                                    public void onWrongExpectedRevision(long streamRevision) {
                                        span.addEvent("exception", Attributes.of(
                                                AttributeKey.stringKey("exception.type"), ErrorCase.STREAM_REVISION_CONFLICT.toString(),
                                                AttributeKey.longKey("exception.revision"), streamRevision
                                        ));
                                    }

                                    @Override
                                    public void onAccessDenied(io.kurrentdb.protocol.streams.v2.ErrorDetails.AccessDenied detail) {
                                        span.addEvent("exception", Attributes.of(
                                                AttributeKey.stringKey("exception.type"), ErrorCase.ACCESS_DENIED.toString()
                                        ));
                                    }

                                    @Override
                                    public void onStreamDeleted() {
                                        span.addEvent("exception", Attributes.of(
                                                AttributeKey.stringKey("exception.type"), ErrorCase.STREAM_DELETED.toString()
                                        ));
                                    }

                                    @Override
                                    public void onTransactionMaxSizeExceeded(int maxSize) {
                                        span.addEvent("exception", Attributes.of(
                                                AttributeKey.stringKey("exception.type"), ErrorCase.TRANSACTION_MAX_SIZE_EXCEEDED.toString(),
                                                AttributeKey.longKey("exception.maxSize"), (long) maxSize
                                        ));
                                    }
                                });
                            }
                            span.setStatus(StatusCode.ERROR);
                            span.end();
                        } else if (result.getSuccesses().isPresent()) {
                            span.setStatus(StatusCode.OK);
                            span.end();
                        }

                        return result;
                    }
                });
    }

    static void traceSubscribe(Runnable tracedOperation, String subscriptionId, ManagedChannel channel,
                               KurrentDBClientSettings settings,
                               UserCredentials optionalCallCredentials, RecordedEvent event) {
        if (event == null) {
            tracedOperation.run();
            return;
        }

        SpanContext remoteParentContext = tryExtractTracingContext(event.getUserMetadata());

        if (remoteParentContext == null) {
            tracedOperation.run();
            return;
        }

        Span span = createSpan(
                ClientTelemetryConstants.Operations.SUBSCRIBE,
                SpanKind.CONSUMER,
                remoteParentContext,
                ClientTelemetryTags.builder()
                        .withRequiredTag(ClientTelemetryAttributes.KurrentDB.STREAM, event.getStreamId())
                        .withRequiredTag(ClientTelemetryAttributes.KurrentDB.SUBSCRIPTION_ID, subscriptionId)
                        .withRequiredTag(ClientTelemetryAttributes.KurrentDB.EVENT_ID, event.getEventId().toString())
                        .withRequiredTag(ClientTelemetryAttributes.KurrentDB.EVENT_TYPE, event.getEventType())
                        .withServerTagsFromGrpcChannel(channel)
                        .withServerTagsFromClientSettings(settings)
                        .withOptionalDatabaseUserTag(settings.getDefaultCredentials())
                        .withOptionalDatabaseUserTag(optionalCallCredentials)
                        .build());

        try (Scope scope = span.makeCurrent()) {
            tracedOperation.run();
            span.setStatus(StatusCode.OK);
        } catch (Throwable t) {
            span.recordException(t);
            span.setStatus(StatusCode.ERROR);
            throw t;
        } finally {
            span.end();
        }
    }

    static Span createSpan(String operationName, SpanKind spanKind, SpanContext parentContext,
                           ClientTelemetryTags customAttributes) {
        SpanBuilder spanBuilder = getTracer().spanBuilder(operationName).setSpanKind(spanKind);

        if (parentContext != null)
            spanBuilder.setParent(Context.current().with(Span.wrap(parentContext)));

        ClientTelemetryTags attributes = new ClientTelemetryTags(DEFAULT_ATTRIBUTES) {{
            put(ClientTelemetryAttributes.Database.OPERATION, operationName);

            if (customAttributes != null)
                putAll(customAttributes);
        }};

        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            String value = entry.getValue();
            if (value == null) continue;

            spanBuilder.setAttribute(entry.getKey(), value);
        }

        return spanBuilder.startSpan();
    }
}
