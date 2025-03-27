package io.kurrent.dbclient.samples.opentelemetry;

import io.kurrent.dbclient.*;
import io.kurrent.dbclient.samples.TestEvent;
// region import-required-packages
import io.opentelemetry.exporter.logging.LoggingSpanExporter;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
// endregion import-required-packages

import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static io.opentelemetry.semconv.ServiceAttributes.SERVICE_NAME;

public class Instrumentation {
    private static void tracing(KurrentDBClient client) throws ExecutionException, InterruptedException {
        Resource resource = Resource.getDefault().toBuilder()
                .put(SERVICE_NAME, "sample")
                .build();

        // region setup-exporter
        LoggingSpanExporter consoleExporter = LoggingSpanExporter.create();

        OtlpGrpcSpanExporter jaegarExporter = OtlpGrpcSpanExporter.builder()
                .setEndpoint("http://localhost:4317")
                .build();

        SdkTracerProvider sdkTracerProvider = SdkTracerProvider.builder()
                .addSpanProcessor(SimpleSpanProcessor.create(jaegarExporter))
                .addSpanProcessor(SimpleSpanProcessor.create(consoleExporter))
                .setResource(resource)
                .build();
        // endregion setup-exporter

        // region register-instrumentation
        OpenTelemetrySdk.builder()
                .setTracerProvider(sdkTracerProvider)
                .buildAndRegisterGlobal();
        // endregion register-instrumentation

        // region setup-client-for-tracing
        EventData eventData = EventData
                .builderAsJson(
                        UUID.randomUUID(),
                        "some-event",
                        new TestEvent(
                                "1",
                                "some value"
                        ))
                .build();
        // endregion setup-client-for-tracing

        AppendToStreamOptions options = AppendToStreamOptions.get()
                .streamState(StreamState.any());

        WriteResult appendResult = client.appendToStream("some-stream", options, eventData)
                .get();
    }
}
