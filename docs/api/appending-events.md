---
order: 2
---

# Appending events

When you start working with KurrentDB, your application streams are empty. The first meaningful operation is to add one or more events to the database using this API.

::: tip
Check the [Getting Started](getting-started.md) guide to learn how to configure and use the client SDK.
:::

## Append your first event

The simplest way to append an event to KurrentDB is to create an `EventData` object and call `appendToStream` method.

```java {32-43}
class OrderPlaced {
    private String orderId;
    private String customerId;
    private double totalAmount;
    private String status;

    public OrderPlaced(String orderId, String customerId, double totalAmount, String status) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.totalAmount = totalAmount;
        this.status = status;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public String getStatus() {
        return status;
    }
}


EventData eventData = EventData
        .builderAsJson(
                UUID.randomUUID(),
                "OrderPlaced",
                new OrderPlaced("order-456", "customer-789", 249.99, "confirmed"))
        .build();

AppendToStreamOptions options = AppendToStreamOptions.get()
        .streamState(StreamState.noStream());

client.appendToStream("orders", options, eventData)
        .get();

```

`appendToStream` takes a collection or a single object that can be serialized in JSON or binary format, which allows you to save more than one event in a single batch.
 
Outside the example above, other options exist for dealing with different scenarios. 

::: tip
If you are new to Event Sourcing, please study the [Handling concurrency](#handling-concurrency) section below.
:::

## Working with EventData

Events appended to KurrentDB must be wrapped in an `EventData` object. This allows you to specify the event's content, the type of event, and whether it's in JSON format. In its simplest form, you need three arguments: **eventId**, **eventType**, and **eventData**.

### eventId

This takes the format of a `UUID` and is used to uniquely identify the event you are trying to append. If two events with the same `UUID` are appended to the same stream in quick succession, KurrentDB will only append one of the events to the stream. 

For example, the following code will only append a single event:

```java {3,15-16}
EventData eventData = EventData
        .builderAsJson(
                UUID.randomUUID(),
                "OrderPlaced",
                new OrderPlaced("order-456", "customer-789", 249.99, "confirmed"))
        .build();

AppendToStreamOptions options = AppendToStreamOptions.get()
        .streamState(StreamState.any());

client.appendToStream("orders", options, eventData)
        .get();

// attempt to append the same event again
client.appendToStream("orders", options, eventData)
        .get();
```

### eventType

Each event should be supplied with an event type. This unique string is used to identify the type of event you are saving. 

It is common to see the explicit event code type name used as the type as it makes serialising and de-serialising of the event easy. However, we recommend against this as it couples the storage to the type and will make it more difficult if you need to version the event at a later date.

### eventData

Representation of your event data. It is recommended that you store your events as JSON objects. This allows you to take advantage of all of KurrentDB's functionality, such as projections. That said, you can save events using whatever format suits your workflow. Eventually, the data will be stored as encoded bytes.

### userMetadata

Storing additional information alongside your event that is part of the event itself is standard practice. This can be correlation IDs, timestamps, access information, etc. KurrentDB allows you to store a separate byte array containing this information to keep it separate.

### contentType

The content type indicates whether the event is stored as JSON or binary format. This is automatically set when using the builder methods like `builderAsJson()` or `builderAsBinary()`.

## Handling concurrency

When appending events to a stream, you can supply a *stream state*. Your client uses this to inform KurrentDB of the state or version you expect the stream to be in when appending an event. If the stream isn't in that state, an exception will be thrown. 

For example, if you try to append the same record twice, expecting both times that the stream doesn't exist, you will get an exception on the second:

```java
EventData eventDataOne = EventData
        .builderAsJson(
                UUID.randomUUID(),
                "OrderPlaced",
                new OrderPlaced("order-456", "customer-789", 249.99, "confirmed"))
        .build();

EventData eventDataTwo = EventData
        .builderAsJson(
                UUID.randomUUID(),
                "OrderPlaced",
                new OrderPlaced("order-457", "customer-789", 249.99, "confirmed"))
        .build();

AppendToStreamOptions options = AppendToStreamOptions.get()
        .streamState(StreamState.noStream());

client.appendToStream("no-stream-stream", options, eventDataOne)
        .get();

// attempt to append the same event again
client.appendToStream("no-stream-stream", options, eventDataTwo)
        .get();
```

There are several available expected revision options: 
- `StreamState.any()` - No concurrency check
- `StreamState.noStream()` - Stream should not exist
- `StreamState.streamExists()` - Stream should exist
- `StreamState.streamRevision(long revision)` - Stream should be at specific revision

This check can be used to implement optimistic concurrency. When retrieving a
stream from KurrentDB, note the current version number. When you save it back,
you can determine if somebody else has modified the record in the meantime.

First, let's define the event classes for our ecommerce example:

```java
public class PaymentProcessed {
    private String orderId;
    private String paymentId;
    private double amount;
    private String paymentMethod;

    public PaymentProcessed(String orderId, String paymentId, double amount, String paymentMethod) {
        this.orderId = orderId;
        this.paymentId = paymentId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
    }
    // getters omitted for brevity
}

public class OrderCancelled {
    private String orderId;
    private String reason;
    private String comment;

    public OrderCancelled(String orderId, String reason, String comment) {
        this.orderId = orderId;
        this.reason = reason;
        this.comment = comment;
    }
    // getters omitted for brevity
}
```

Now, here's how to implement optimistic concurrency control:

```java
ReadStreamOptions readOptions = ReadStreamOptions.get()
        .forwards()
        .fromStart();

ReadResult result = client.readStream("order-12345", readOptions)
        .get();

// Get the current revision to use for optimistic concurrency
long currentRevision = result.getLastStreamPosition();

// Two concurrent operations trying to update the same order
EventData paymentProcessedEvent = EventData
        .builderAsJson(
                UUID.randomUUID(),
                "PaymentProcessed",
                new PaymentProcessed("order-12345", "payment-789", 149.99, "VISA"))
        .build();

EventData orderCancelledEvent = EventData
        .builderAsJson(
                UUID.randomUUID(),
                "OrderCancelled",
                new OrderCancelled("order-12345", "customer-request", "Customer changed mind"))
        .build();

// Process payment (succeeds)
AppendToStreamOptions appendOptions = AppendToStreamOptions.get()
        .streamState(currentRevision);

WriteResult paymentResult = client.appendToStream("order-12345", appendOptions, paymentProcessedEvent)
        .get();

// Cancel order (fails due to concurrency conflict)
AppendToStreamOptions cancelOptions = AppendToStreamOptions.get()
        .streamState(currentRevision);

client.appendToStream("order-12345", cancelOptions, orderCancelledEvent)
        .get();
```

## User credentials

You can provide user credentials to append the data as follows. This will override the default credentials set on the connection.

```java
UserCredentials credentials = new UserCredentials("admin", "changeit");

AppendToStreamOptions options = AppendToStreamOptions.get()
        .authenticated(credentials);

client.appendToStream("some-stream", options, eventData)
        .get();
```

## Append to multiple streams

::: note
This feature is only available in KurrentDB 25.1 and later.
:::

You can append events to multiple streams in a single atomic operation. Either all streams are updated, or the entire operation fails.

::: warning
Metadata must be a valid JSON object, using string keys and string values only.
Binary metadata is not supported in this version to maintain compatibility with
KurrentDB's metadata handling. This restriction will be lifted in the next major
release.
:::

```java
JsonMapper mapper = new JsonMapper();

Map<String, Object> metadata = new HashMap<>();
metadata.put("source", "OrderProcessingSystem");

byte[] metadataBytes = mapper.writeValueAsBytes(metadata);

EventData orderEvent = EventData
        .builderAsJson("OrderCreated", mapper.writeValueAsBytes(new OrderCreated("12345", 99.99)))
        .metadataAsBytes(metadataBytes)
        .build();

EventData inventoryEvent = EventData
        .builderAsJson("ProductPurchased", mapper.writeValueAsBytes(new ProductPurchased("ABC123", 2, 19.99)))
        .metadataAsBytes(metadataBytes)
        .build();

List<AppendStreamRequest> requests = Arrays.asList(
        new AppendStreamRequest(
                "order-stream-1",
                Collections.singletonList(orderEvent).iterator(),
                StreamState.any()
        ),
        new AppendStreamRequest(
                "product-stream-1",
                Collections.singletonList(inventoryEvent).iterator(),
                StreamState.any()
        )
);

MultiAppendWriteResult result = client.multiStreamAppend(requests.iterator()).get();
```

