---
order: 3
head:
  - - title
    - {}
    - Reading Events | Java | Clients | EventStore Docs
---

# Reading Events

EventStoreDB provides two primary methods for reading events: reading from an
individual stream to retrieve events from a specific named stream, or reading
from the `$all` stream to access all events across the entire event store.

Events in EventStoreDB are organized within individual streams and use two
distinct positioning systems to track their location. The **revision number** is
a 64-bit signed integer (`long`) that represents the sequential position of an
event within its specific stream. Events are numbered starting from 0, with each
new event receiving the next sequential revision number (0, 1, 2, 3...). The
**global position** represents the event's location in EventStoreDB's global
transaction log and consists of two coordinates: the `commit` position (where
the transaction was committed in the log) and the `prepare` position (where the
transaction was initially prepared).

These positioning identifiers are essential for reading operations, as they
allow you to specify exactly where to start reading from within a stream or
across the entire event store.

## Reading from a stream

You can read all the events or a sample of the events from individual streams,
starting from any position in the stream, and can read either forward or
backward. It is only possible to read events from a single stream at a time. You
can read events from the global event log, which spans across streams. Learn
more about this process in the [Read from `$all`](#reading-from-the-all-stream)
section below.

### Reading forwards

The simplest way to read a stream forwards is to supply a stream name, read
direction, and revision from which to start. The revision can be specified in several ways:

- Use `fromStart()` to begin from the very beginning of the stream
- Use `fromEnd()` to begin from the current end of the stream
- Use `fromRevision(long revision)` with a specific revision number (64-bit signed integer)

```java{3}
ReadStreamOptions options = ReadStreamOptions.get()
        .forwards()
        .fromStart();

ReadResult result = client.readStream("orders", options)
        .get();
```

You can also start reading from a specific revision in the stream:

```java{3}
ReadStreamOptions options = ReadStreamOptions.get()
        .forwards()
        .fromRevision(10);

ReadResult result = client.readStream("orders", options)
        .get();
```

You can then iterate synchronously through the result:

```java
import com.fasterxml.jackson.databind.json.JsonMapper;

JsonMapper mapper = new JsonMapper();

for (ResolvedEvent resolvedEvent : result.getEvents()) {
    RecordedEvent recordedEvent = resolvedEvent.getOriginalEvent();
    System.out.println(mapper.writeValueAsString(recordedEvent.getEventData()));
}
```

There are a number of additional arguments you can provide when reading a stream.

#### maxCount

Passing in the max count allows you to limit the number of events that returned.

```java{4}
ReadStreamOptions options = ReadStreamOptions.get()
        .forwards()
        .fromRevision(10)
        .maxCount(20);
```

#### resolveLinkTos

When using projections to create new events you can set whether the generated events are pointers to existing events. Setting this value to true will tell EventStoreDB to return the event as well as the event linking to it.

```java{4}
ReadStreamOptions options = ReadStreamOptions.get()
        .forwards()
        .fromStart()
        .resolveLinkTos();

ReadResult result = client.readStream("orders", options)
        .get();
```

#### userCredentials

The credentials used to read the data can be used by the subscription as follows. This will override the default credentials set on the connection.

```java{4}
ReadStreamOptions options = ReadStreamOptions.get()
        .forwards()
        .fromStart()
        .authenticated("admin", "changeit");

ReadResult result = client.readStream("orders", options)
        .get();
```

### Reading backwards

In addition to reading a stream forwards, streams can be read backwards. To read all the events backwards, set the *stream position* to the end:

```java{4}
JsonMapper mapper = new JsonMapper();

ReadStreamOptions options = ReadStreamOptions.get()
      .backwards()
      .fromEnd();

ReadResult result = client.readStream("orders", options)
        .get();

for (ResolvedEvent resolvedEvent : result.getEvents()) {
    RecordedEvent recordedEvent = resolvedEvent.getOriginalEvent();
    System.out.println(mapper.writeValueAsString(recordedEvent.getEventData()));
}
```

:::tip
Read one event backwards to find the last position in the stream.
:::

### Checking if the stream exists

Reading a stream returns a `ReadStreamResult`, which contains a property `ReadState`. This property can have the value `StreamNotFound` or `Ok`.

It is important to check the value of this field before attempting to iterate an empty stream, as it will throw an exception. 

For example:

```java{11-15}
ReadStreamOptions options = ReadStreamOptions.get()
        .forwards()
        .fromRevision(10)
        .maxCount(20);

ReadResult result = null;
try {
    result = client.readStream("some-stream", options)
            .get();
} catch (ExecutionException e) {
    Throwable innerException = e.getCause();

    if (innerException instanceof StreamNotFoundException) {
        return;
    }
}
```

## Reading from the $all stream

Reading from the `$all` stream is similar to reading from an individual stream, but please note there are differences. One significant difference is the need to provide admin user account credentials to read from the `$all` stream.  Additionally, you need to provide a transaction log position instead of a stream revision when reading from the `$all` stream.

### Reading forwards

The simplest way to read the `$all` stream forwards is to supply a read
direction and the transaction log position from which you want to start. The
transaction log position can be specified in several ways:

- Use `fromStart()` to begin from the very beginning of the transaction log
- Use `fromEnd()` to begin from the current end of the transaction log  
- Use `fromPosition(Position position)` with a specific `Position` object containing commit and prepare coordinates

```java{2-3}
ReadAllOptions options = ReadAllOptions.get()
        .forwards()
        .fromStart();

ReadResult result = client.readAll(options)
        .get();
```

You can also start reading from a specific position in the transaction log:

```java{1,4-5}
Position position = new Position(1000, 1000);

ReadAllOptions options = ReadAllOptions.get()
        .forwards()
        .fromPosition(position);

ReadResult result = client.readAll(options)
        .get();
```

You can then iterate synchronously through the result:

```java
import com.fasterxml.jackson.databind.json.JsonMapper;

JsonMapper mapper = new JsonMapper();

for (ResolvedEvent resolvedEvent : result.getEvents()) {
    RecordedEvent recordedEvent = resolvedEvent.getOriginalEvent();
    System.out.println(mapper.writeValueAsString(recordedEvent.getEventData()));
}
```

There are a number of additional arguments you can provide when reading the `$all` stream.

#### maxCount

Passing in the max count allows you to limit the number of events that returned.

```java{4}
ReadAllOptions options = ReadAllOptions.get()
        .forwards()
        .fromRevision(10)
        .maxCount(20);
```

#### resolveLinkTos

When using projections to create new events you can set whether the generated events are pointers to existing events. Setting this value to true will tell EventStoreDB to return the event as well as the event linking to it.

```java{4}
ReadAllOptions options = ReadAllOptions.get()
        .forwards()
        .fromStart()
        .resolveLinkTos();

ReadResult result = client.readAll(options)
        .get();
```

#### userCredentials
The credentials used to read the data can be used by the subscription as follows. This will override the default credentials set on the connection.

```java{4}
ReadAllOptions options = ReadAllOptions.get()
        .forwards()
        .fromStart()
        .authenticated("admin", "changeit");

ReadResult result = client.readAll(options)
        .get();
```

### Reading backwards

In addition to reading the `$all` stream forwards, it can be read backwards. To
read all the events backwards, set the _direction_ to the `Backwards`:

```java{2}
ReadAllOptions options = ReadAllOptions.get()
        .backwards()
        .fromEnd();

ReadResult result = client.readAll(options)
        .get();
```

:::tip
Read one event backwards to find the last position in the `$all` stream.
:::

### Handling system events

EventStoreDB will also return system events when reading from the `$all` stream. In most cases you can ignore these events.

All system events begin with `$` or `$$` and can be easily ignored by checking the `eventType` property.

```java{10-12}
ReadAllOptions options = ReadAllOptions.get()
        .forwards()
        .fromStart();

ReadResult result = client.readAll(options)
        .get();

for (ResolvedEvent resolvedEvent : result.getEvents()) {
    RecordedEvent recordedEvent = resolvedEvent.getOriginalEvent();
    if (!recordedEvent.getEventType().startsWith("$")) {
      // Process the event
    }
}
```

## Java Reactive Streams 

The Java Reactive Streams API allows you to read events in a non-blocking manner, which is particularly useful for applications that require high throughput and low latency. The reactive API provides a way to subscribe to streams of events and process them as they arrive.

::: tabs#java
@tab Reading from a stream
```java
import org.reactivestreams.Subscriber;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscription;
import java.util.concurrent.CountDownLatch;

ReadStreamOptions options = ReadStreamOptions.get()
        .forwards()
        .fromStart();

Publisher<ReadMessage> publisher = client.readStreamReactive("orders", options);

final CountDownLatch latch = new CountDownLatch(1);
publisher.subscribe(new Subscriber<ReadMessage>() {
    @Override
    public void onSubscribe(Subscription subscription) {
    }

    @Override
    public void onNext(ReadMessage readMessage) {
        RecordedEvent event = readMessage.getEvent().getOriginalEvent();
        // Process the event
        System.out.println("Event: " + event.getEventType());
    }

    @Override
    public void onError(Throwable throwable) {
        // Handle error
        latch.countDown();
    }

    @Override
    public void onComplete() {
        latch.countDown();
    }
});

latch.await();
```
@tab Reading from $all
```java
import org.reactivestreams.Subscriber;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscription;
import java.util.concurrent.CountDownLatch;

ReadAllOptions options = ReadAllOptions.get()
        .forwards()
        .fromStart();

Publisher<ReadMessage> publisher = client.readAllReactive(options);

final CountDownLatch latch = new CountDownLatch(1);
publisher.subscribe(new Subscriber<ReadMessage>() {
    @Override
    public void onSubscribe(Subscription subscription) {
    }

    @Override
    public void onNext(ReadMessage readMessage) {
        RecordedEvent event = readMessage.getEvent().getOriginalEvent();
        // Filter out system events if needed
        if (!event.getEventType().startsWith("$")) {
            System.out.println("Event: " + event.getEventType());
        }
    }

    @Override
    public void onError(Throwable throwable) {
        // Handle error
        latch.countDown();
    }

    @Override
    public void onComplete() {
        latch.countDown();
    }
});

latch.await();
```
:::