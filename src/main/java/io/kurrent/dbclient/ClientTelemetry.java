package io.kurrent.dbclient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.grpc.ManagedChannel;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.*;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.context.propagation.TextMapSetter;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.BiFunction;

class ClientTelemetry {
    private static final ClientTelemetryTags DEFAULT_ATTRIBUTES = new ClientTelemetryTags() {{
        put(ClientTelemetryAttributes.Database.SYSTEM, ClientTelemetryConstants.INSTRUMENTATION_NAME);
    }};

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final TextMapSetter<ObjectNode> METADATA_SETTER =
            (userMetadata, key, value) -> userMetadata.put("$" + key, value);

    private static final TextMapGetter<ObjectNode> METADATA_GETTER = new TextMapGetter<ObjectNode>() {
        @Override
        public Iterable<String> keys(ObjectNode userMetadata) {
            return Arrays.asList("traceparent", "tracestate");
        }

        @Override
        public String get(ObjectNode userMetadata, String key) {
            return getTextField(userMetadata, "$" + key);
        }
    };

    private static String getTextField(ObjectNode userMetadata, String fieldName) {
        JsonNode field = userMetadata.get(fieldName);
        return field != null && field.isTextual() ? field.asText() : null;
    }

    private static Tracer getTracer() {
        return GlobalOpenTelemetry.getTracer(
                ClientTelemetry.class.getPackage().getName(),
                ClientTelemetry.class.getPackage().getImplementationVersion());
    }

    static List<EventData> tryInjectTracingContext(Span span, List<EventData> events) {
        if (!span.getSpanContext().isValid())
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

    static byte[] tryInjectTracingContext(Span span, byte[] userMetadataBytes) {
        if (!span.getSpanContext().isValid())
            return userMetadataBytes;

        try {
            ObjectNode userMetadata = userMetadataBytes != null
                    ? OBJECT_MAPPER.readValue(userMetadataBytes, ObjectNode.class)
                    : OBJECT_MAPPER.createObjectNode();

            userMetadata.remove(ClientTelemetryConstants.Metadata.TRACE_STATE);

            W3CTraceContextPropagator.getInstance()
                    .inject(Context.root().with(span), userMetadata, METADATA_SETTER);

            // Legacy fields carry no sampling flag, so older readers would treat an
            // unsampled trace as sampled. Only write them when sampled.
            if (span.getSpanContext().isSampled()) {
                userMetadata.put(ClientTelemetryConstants.Metadata.TRACE_ID, span.getSpanContext().getTraceId());
                userMetadata.put(ClientTelemetryConstants.Metadata.SPAN_ID, span.getSpanContext().getSpanId());
            } else {
                userMetadata.remove(ClientTelemetryConstants.Metadata.TRACE_ID);
                userMetadata.remove(ClientTelemetryConstants.Metadata.SPAN_ID);
            }

            return OBJECT_MAPPER.writeValueAsBytes(userMetadata);
        } catch (Throwable t) {
            // User metadata may not be a valid JSON object, or not JSON altogether.
            return userMetadataBytes;
        }
    }

    static SpanContext tryExtractTracingContext(byte[] userMetadataBytes) {
        if (userMetadataBytes == null)
            return null;

        try {
            ObjectNode userMetadata = OBJECT_MAPPER.readValue(userMetadataBytes, ObjectNode.class);

            Context extracted = W3CTraceContextPropagator.getInstance()
                    .extract(Context.root(), userMetadata, METADATA_GETTER);

            SpanContext traceParentContext = Span.fromContext(extracted).getSpanContext();
            if (traceParentContext.isValid())
                return traceParentContext;

            return tryExtractLegacyTracingContext(userMetadata);
        } catch (Throwable t) {
            return null;
        }
    }

    private static SpanContext tryExtractLegacyTracingContext(ObjectNode userMetadata) {
        String traceId = getTextField(userMetadata, ClientTelemetryConstants.Metadata.TRACE_ID);
        String spanId = getTextField(userMetadata, ClientTelemetryConstants.Metadata.SPAN_ID);

        if (traceId == null || spanId == null)
            return null;

        if (!TraceId.isValid(traceId) || !SpanId.isValid(spanId))
            return null;

        return SpanContext.createFromRemoteParent(traceId, spanId, TraceFlags.getSampled(),
                TraceState.getDefault());
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

    static CompletableFuture<MultiStreamAppendResponse> traceMultiStreamAppend(
            BiFunction<WorkItemArgs, Iterator<AppendStreamRequest>, CompletableFuture<MultiStreamAppendResponse>> multiAppendOperation,
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
                        span.setStatus(StatusCode.OK);
                        span.end();
                        return result;
                    }
                });
    }

    static CompletableFuture<AppendRecordsResponse> traceAppendRecords(
            BiFunction<WorkItemArgs, List<AppendRecord>, CompletableFuture<AppendRecordsResponse>> appendRecordsOperation,
            WorkItemArgs args,
            List<AppendRecord> records, KurrentDBClientSettings settings) {

        Span span = createSpan(
                ClientTelemetryConstants.Operations.MULTI_APPEND,
                SpanKind.CLIENT,
                null,
                ClientTelemetryTags.builder()
                        .withServerTagsFromGrpcChannel(args.getChannel())
                        .withServerTagsFromClientSettings(settings)
                        .withOptionalDatabaseUserTag(settings.getDefaultCredentials())
                        .build());

        List<AppendRecord> recordsWithTracing = new ArrayList<>();
        for (AppendRecord record : records) {
            List<EventData> traced = tryInjectTracingContext(span, Collections.singletonList(record.getRecord()));
            recordsWithTracing.add(new AppendRecord(record.getStream(), traced.get(0)));
        }

        return appendRecordsOperation.apply(args, recordsWithTracing)
                .handle((result, throwable) -> {
                    if (throwable != null) {
                        span.setStatus(StatusCode.ERROR);
                        span.recordException(throwable);
                        span.end();
                        throw new CompletionException(throwable);
                    } else {
                        span.setStatus(StatusCode.OK);
                        span.end();
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
