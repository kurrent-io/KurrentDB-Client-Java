---
order: 8
head:
  - - title
    - {}
    - Observability | Java | Clients | Kurrent Docs
---

# Observability

The Java client provides observability capabilities through OpenTelemetry
integration. This enables you to monitor, trace, and troubleshoot your event
store operations with distributed tracing support.

## Prerequisites

You'll need to add OpenTelemetry dependencies to your project. Add these to your
Maven `pom.xml` or Gradle `build.gradle`:

::: tabs#distribution
@tab Maven
```xml
<dependencies>
    <!-- OpenTelemetry SDK -->
    <dependency>
        <groupId>io.opentelemetry</groupId>
        <artifactId>opentelemetry-sdk</artifactId>
        <version>1.40.0</version>
    </dependency>
    
    <!-- For console/logging output -->
    <dependency>
        <groupId>io.opentelemetry</groupId>
        <artifactId>opentelemetry-exporter-logging</artifactId>
        <version>1.40.0</version>
    </dependency>
    
    <!-- For OTLP (OpenTelemetry Protocol) -->
    <dependency>
        <groupId>io.opentelemetry</groupId>
        <artifactId>opentelemetry-exporter-otlp</artifactId>
        <version>1.40.0</version>
    </dependency>
    
    <!-- Semantic conventions -->
    <dependency>
        <groupId>io.opentelemetry.semconv</groupId>
        <artifactId>opentelemetry-semconv</artifactId>
        <version>1.25.0-alpha</version>
    </dependency>
</dependencies>
```
@tab Gradle
```groovy
dependencies {
    implementation 'io.opentelemetry:opentelemetry-sdk:1.40.0'
    implementation 'io.opentelemetry:opentelemetry-exporter-logging:1.40.0'
    implementation 'io.opentelemetry:opentelemetry-exporter-otlp:1.40.0'
    implementation 'io.opentelemetry.semconv:opentelemetry-semconv:1.25.0-alpha'
}
```
:::

## Basic Configuration

Configure OpenTelemetry by creating and registering the SDK with appropriate
exporters. Here's a minimal setup:

```java
import com.eventstore.dbclient.*;
import io.opentelemetry.exporter.logging.LoggingSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;

import static io.opentelemetry.semconv.ServiceAttributes.SERVICE_NAME;

public class EventStoreObservability {
    public static void main(String[] args) {
        // Configure resource with service name
        Resource resource = Resource.getDefault().toBuilder()
                .put(SERVICE_NAME, "my-eventstore-app")
                .build();

        // Create console exporter
        LoggingSpanExporter consoleExporter = LoggingSpanExporter.create();

        // Configure tracer provider
        SdkTracerProvider sdkTracerProvider = SdkTracerProvider.builder()
                .addSpanProcessor(SimpleSpanProcessor.create(consoleExporter))
                .setResource(resource)
                .build();

        // Register OpenTelemetry SDK globally
        OpenTelemetrySdk.builder()
                .setTracerProvider(sdkTracerProvider)
                .buildAndRegisterGlobal();

        // Your KurrentDB client operations will now be traced
        KurrentDBClientSettings settings = KurrentDBConnectionString
                .parseOrThrow("kurrentdb://localhost:2113?tls=false");
        KurrentDBClient client = KurrentDBClient.create(settings);
    }
}
```

## Trace Exporters

OpenTelemetry supports various exporters to send trace data to different
observability platforms. You can find a list of available exporters in the
[OpenTelemetry Registry](https://opentelemetry.io/ecosystem/registry/?component=exporter&language=java).

You can configure multiple exporters simultaneously:

```java
import io.opentelemetry.exporter.logging.LoggingSpanExporter;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;

import static io.opentelemetry.semconv.ServiceAttributes.SERVICE_NAME;

public class MultipleExporters {
    public static void configureOpenTelemetry() {
        Resource resource = Resource.getDefault().toBuilder()
                .put(SERVICE_NAME, "my-eventstore-app")
                .build();

        // Console/logging exporter
        LoggingSpanExporter consoleExporter = LoggingSpanExporter.create();

        // OTLP exporter for Jaeger/other OTLP-compatible backends
        OtlpGrpcSpanExporter otlpExporter = OtlpGrpcSpanExporter.builder()
                .setEndpoint("http://localhost:4317")
                .build();

        // Configure tracer provider with multiple exporters
        SdkTracerProvider sdkTracerProvider = SdkTracerProvider.builder()
                .addSpanProcessor(SimpleSpanProcessor.create(consoleExporter))
                .addSpanProcessor(SimpleSpanProcessor.create(otlpExporter))
                .setResource(resource)
                .build();

        // Register globally
        OpenTelemetrySdk.builder()
                .setTracerProvider(sdkTracerProvider)
                .buildAndRegisterGlobal();
    }
}
```

For detailed configuration options, refer to the
[OpenTelemetry Java documentation](https://opentelemetry.io/docs/languages/java/).

## Understanding Traces

### What Gets Traced

The Java client automatically creates traces for append, catch-up and persistent
subscription operations when OpenTelemetry is configured globally.

### Trace Attributes

Each trace includes metadata to help with debugging and monitoring:

| Attribute                         | Description                            | Example                               |
| --------------------------------- | -------------------------------------- | ------------------------------------- |
| `db.user`                         | Database user name                     | `admin`                               |
| `db.system`                       | Database system identifier             | `eventstoredb`                        |
| `db.operation`                    | Type of operation performed            | `streams.append`, `streams.subscribe` |
| `db.eventstoredb.stream`          | Stream name or identifier              | `user-events-123`                     |
| `db.eventstoredb.subscription.id` | Subscription identifier                | `user-events-123-sub`                 |
| `db.eventstoredb.event.id`        | Event identifier                       | `event-456`                           |
| `db.eventstoredb.event.type`      | Event type identifier                  | `user.created`                        |
| `server.address`                  | KurrentDB server address            | `localhost`                           |
| `server.port`                     | KurrentDB server port               | `2113`                                |
| `exception.type`                  | Exception type if an error occurred    |                                       |
| `exception.message`               | Exception message if an error occurred |                                       |
| `exception.stacktrace`            | Stack trace of the exception           |                                       |
