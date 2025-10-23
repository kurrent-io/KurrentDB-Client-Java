---
order: 1
head:
  - - title
    - {}
    - Getting Started | Java | Clients | KurrentDB Docs
---

# Getting started

This guide will help you get started with KurrentDB in your Java application.
It covers the basic steps to connect to KurrentDB, create events, append them
to streams, and read them back.

## Required packages

Add the `kurrentdb-client` dependency to your project:

::: tabs
@tab gradle
```groovy
implementation 'io.kurrent:kurrentdb-client:1.1.x'
```
@tab maven
```xml
<dependency>
    <groupId>io.kurrent</groupId>
    <artifactId>kurrentdb-client</artifactId>
    <version>1.1.x</version>
</dependency>
```
:::

## Connecting to KurrentDB

To connect your application to KurrentDB, you need to configure and create a client instance.

::: tip Insecure clusters
The recommended way to connect to KurrentDB is using secure mode (which is
the default). However, if your KurrentDB instance is running in insecure
mode, you must explicitly set `tls=false` in your connection
string or client configuration.
:::

KurrentDB uses connection strings to configure the client connection. The connection string supports two protocols:

- **`kurrentdb://`** - for connecting directly to specific node endpoints (single node or multi-node cluster with explicit endpoints)
- **`kurrentdb+discover://`** - for connecting using cluster discovery via DNS or gossip endpoints

When using `kurrentdb://`, you specify the exact endpoints to connect to. The client will connect directly to these endpoints. For multi-node clusters, you can specify multiple endpoints separated by commas, and the client will query each node's Gossip API to get cluster information, then picks a node based on the URI's node preference.

With `kurrentdb+discover://`, the client uses cluster discovery to find available nodes. This is particularly useful when you have a DNS A record pointing to cluster nodes or when you want the client to automatically discover the cluster topology.

::: info Gossip support
Since version 22.10, KurrentDB supports gossip on single-node deployments, so
`kurrentdb+discover://` can be used for any topology, including single-node setups.
:::

For cluster connections using discovery, use the following format:

```
kurrentdb+discover://admin:changeit@cluster.dns.name:2113
```

Where `cluster.dns.name` is a DNS `A` record that points to all cluster nodes.

For direct connections to specific endpoints, you can specify individual nodes:

```
kurrentdb://admin:changeit@node1.dns.name:2113,node2.dns.name:2113,node3.dns.name:2113
```

Or for a single node:

```
kurrentdb://admin:changeit@localhost:2113
```

There are a number of query parameters that can be used in the connection string to instruct the cluster how and where the connection should be established. All query parameters are optional.

| Parameter             | Accepted values                                   | Default  | Description                                                                                                                                    |
|-----------------------|---------------------------------------------------|----------|------------------------------------------------------------------------------------------------------------------------------------------------|
| `tls`                 | `true`, `false`                                   | `true`   | Use secure connection, set to `false` when connecting to a non-secure server or cluster.                                                       |
| `connectionName`      | Any string                                        | None     | Connection name                                                                                                                                |
| `maxDiscoverAttempts` | Number                                            | `10`     | Number of attempts to discover the cluster.                                                                                                    |
| `discoveryInterval`   | Number                                            | `100`    | Cluster discovery polling interval in milliseconds.                                                                                            |
| `gossipTimeout`       | Number                                            | `5`      | Gossip timeout in seconds, when the gossip call times out, it will be retried.                                                                 |
| `nodePreference`      | `leader`, `follower`, `random`, `readOnlyReplica` | `leader` | Preferred node role. When creating a client for write operations, always use `leader`.                                                         |
| `tlsVerifyCert`       | `true`, `false`                                   | `true`   | In secure mode, set to `true` when using an untrusted connection to the node if you don't have the CA file available. Don't use in production. |
| `tlsCaFile`           | String, file path                                 | None     | Path to the CA file when connecting to a secure cluster with a certificate that's not signed by a trusted CA.                                  |
| `defaultDeadline`     | Number                                            | None     | Default timeout for client operations, in milliseconds. Most clients allow overriding the deadline per operation.                              |
| `keepAliveInterval`   | Number                                            | `10`     | Interval between keep-alive ping calls, in seconds.                                                                                            |
| `keepAliveTimeout`    | Number                                            | `10`     | Keep-alive ping call timeout, in seconds.                                                                                                      |
| `userCertFile`        | String, file path                                 | None     | User certificate file for X.509 authentication.                                                                                                |
| `userKeyFile`         | String, file path                                 | None     | Key file for the user certificate used for X.509 authentication.                                                                               |
| `feature`             | `dns-lookup`                                      | None     | Enable specific client features. Use `dns-lookup` with `dnsDiscover=true` to resolve hostnames to multiple IP addresses for cluster discovery. |

When connecting to an insecure instance, specify `tls=false` parameter. For example, for a node running locally use `kurrentdb://localhost:2113?tls=false`. Note that usernames and passwords aren't provided there because insecure deployments don't support authentication and authorisation.

## Creating a client

First, create a client and get it connected to the database.

```java
KurrentDBClientSettings settings = KurrentDBConnectionString.parseOrThrow("kurrentdb://localhost:2113?tls=false");
KurrentDBClient client = KurrentDBClient.create(settings);
```

The client instance can be used as a singleton across the whole application. It doesn't need to open or close the connection.

## Creating an event

You can write anything to KurrentDB as events. The client needs a byte array as the event payload. Normally, you'd use a serialized object, and it's up to you to choose the serialization method.

The code snippet below creates an event object instance, serializes it, and adds it as a payload to the `EventData` structure, which the client can then write to the database.

```java
public class OrderPlaced {
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
}

OrderPlaced event = new OrderPlaced("order-456", "customer-789", 249.99, "confirmed");
JsonMapper jsonMapper = new JsonMapper();

EventData eventData = EventData
        .builderAsJson("OrderPlaced", jsonMapper.writeValueAsBytes(event))
        .build();
```

## Appending events

Each event in the database has its own unique identifier (UUID). The database uses it to ensure idempotent writes, but it only works if you specify the stream revision when appending events to the stream.

In the snippet below, we append the event to the stream `orders`.

```java
client.appendToStream("orders", eventData).get();
```

Here we are appending events without checking if the stream exists or if the stream version matches the expected event version. See more advanced scenarios in [appending events documentation](./appending-events.md).

## Reading events

Finally, we can read events back from the `orders` stream.

### Synchronous reading

```java
import java.util.List;

ReadStreamOptions options = ReadStreamOptions.get()
        .forwards()
        .fromStart()
        .maxCount(10);

ReadResult result = client.readStream("orders", options)
        .get();

List<ResolvedEvent> events = result.getEvents();
```

### Asynchronous reading

We also provide an asynchronous API for reading events using Java Reactive Streams.

```java
import org.reactivestreams.Subscriber;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscription;

import java.util.concurrent.CountDownLatch;

ReadStreamOptions options = ReadStreamOptions.get()
        .forwards()
        .fromStart()
        .maxCount(10);

Publisher<ReadMessage> publisher = client.readStreamReactive("orders", options);

final CountDownLatch latch = new CountDownLatch(1);
publisher.subscribe(new Subscriber<ReadMessage>() {
    @Override
    public void onSubscribe(Subscription subscription) {
        // subscription confirmed
    }

    @Override
    public void onNext(ReadMessage readMessage) {
        // Process the event
        RecordedEvent event = readMessage.getEvent().getOriginalEvent();
    }

    @Override
    public void onError(Throwable throwable) {
        // handle error
    }

    @Override
    public void onComplete() {
        latch.countDown();
    }
});

latch.await();
```

When you read events from the stream, you get a collection of `ResolvedEvent`
structures (synchronous) or `ReadMessage` objects (reactive). The event payload
is returned as a byte array and needs to be deserialized. See more advanced
scenarios in [reading events documentation](./reading-events.md).
