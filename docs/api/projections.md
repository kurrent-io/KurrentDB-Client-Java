---
order: 6
title: Projections
head:
  - - title
    - {}
    - Projections | Java | Clients | EventStore Docs
---

# Projection management

The client provides a way to manage projections in EventStoreDB. 

For a detailed explanation of projections, see the [server documentation](@server/features/projections/README.md).

## Creating a client

The Java client provides a `EventStoreDBProjectionManagementClient` that you can use to manage persistent subscriptions.

```java
EventStoreDBClientSettings settings = EventStoreDBConnectionString.parseOrThrow("esdb://localhost:2113?tls=false");
EventStoreDBProjectionManagementClient client = EventStoreDBProjectionManagementClient.create(settings);
```

## Create a projection

Creates a projection that runs until the last event in the store, and then continues processing new events as they are appended to the store. The query parameter contains the JavaScript you want created as a projection.
Projections have explicit names, and you can enable or disable them via this name.

```java
String js =
        "fromAll()" +
        ".when({" +
        "    $init: function() {" +
        "        return {" +
        "            count: 0" +
        "        };" +
        "    }," +
        "    $any: function(s, e) {" +
        "        s.count += 1;" +
        "    }" +
        "})" +
        ".outputState();";

String name = "countEvents_Create_" + java.util.UUID.randomUUID();

client.create(name, js).get();
```

Trying to create projections with the same name will result in an error:

```java
try {
    client.create(name, js).get();
} catch (ExecutionException ex) {
    if (ex.getMessage().contains("Conflict")) {
        System.out.println(name + " already exists");
    }
}
```

## Restart the subsystem

It is possible to restart the entire projection subsystem using the projections management client API. The user must be in the `$ops` or `$admin` group to perform this operation.

```java
client.restartSubsystem().get();
```

## Enable a projection

This Enables an existing projection by name. Once enabled, the projection will
start to process events even after restarting the server or the projection
subsystem. You must have access to a projection to enable it, see the [ACL documentation](@server/security/user-authorization.md).

```java
client.enable("$by_category").get();
```

You can only enable an existing projection. When you try to enable a non-existing projection, you'll get an error:

 ```java
try {
    client.disable("projection that does not exists").get();
} catch (ExecutionException ex) {
    if (ex.getMessage().contains("NotFound")) {
        System.out.println(ex.getMessage());
    }
}
```

## Disable a projection

Disables a projection, this will save the projection checkpoint.
Once disabled, the projection will not process events even after restarting the server or the projection subsystem.
You must have access to a projection to disable it, see the [ACL documentation](@server/security/user-authorization.md).

```java
client.disable("$by_category").get();
```

You can only disable an existing projection. When you try to disable a non-existing projection, you'll get an error:

```java
try {
    client.disable("projection that does not exists").get();
} catch (ExecutionException ex) {
    if (ex.getMessage().contains("NotFound")) {
        System.out.println(ex.getMessage());
    }
}
```

## Delete a projection

```java
// A projection must be disabled to allow it to be deleted.
client.disable(name).get();

// The projection can now be deleted
client.delete(name).get();
```

## Abort a projection

Aborts a projection, this will not save the projection's checkpoint.

```java
client.abort("$by_category").get();
```

You can only abort an existing projection. When you try to abort a non-existing projection, you'll get an error:

```java
try {
    client.abort("projection that does not exists").get();
} catch (ExecutionException ex) {
    if (ex.getMessage().contains("NotFound")) {
        System.out.println(ex.getMessage());
    }
}
```

## Reset a projection

Resets a projection, which causes deleting the projection checkpoint. This will force the projection to start afresh and re-emit events. Streams that are written to from the projection will also be soft-deleted.

```java
client.reset("$by_category").get();
```

Resetting a projection that does not exist will result in an error.

```java
try {
    client.reset("projection that does not exists").get();
} catch (ExecutionException ex) {
    if (ex.getMessage().contains("NotFound")) {
        System.out.println(ex.getMessage());
    }
}
```

## Update a projection

Updates a projection with a given name. The query parameter contains the new JavaScript. Updating system projections using this operation is not supported at the moment.

```java
String name = "countEvents_Update_" + java.util.UUID.randomUUID();
String js =
        "fromAll()" +
                ".when({" +
                "    $init: function() {" +
                "        return {" +
                "            count: 0" +
                "        };" +
                "    }," +
                "    $any: function(s, e) {" +
                "        s.count += 1;" +
                "    }" +
                "})" +
                ".outputState();";

client.create(name, "fromAll().when()").get();
client.update(name, js).get();
```

You can only update an existing projection. When you try to update a non-existing projection, you'll get an error:

```java
try {
    client.update("Update Not existing projection", "fromAll().when()").get();
} catch (ExecutionException ex) {
    if (ex.getMessage().contains("NotFound")) {
        System.out.println("'Update Not existing projection' does not exists and can not be updated");
    }
}
```

## List all projections

Returns a list of all projections, user defined & system projections.
See the [projection details](#projection-details) section for an explanation of the returned values.

```java
List<ProjectionDetails> details = client.list().get();

for (ProjectionDetails detail: details) {
    System.out.println(
        detail.getName() + ", " +
        detail.getStatus() + ", " +
        detail.getCheckpointStatus() + ", " +
        detail.getMode() + ", " +
        detail.getProgress());
}
```

## List continuous projections

Returns a list of all continuous projections.
See the [projection details](#projection-details) section for an explanation of the returned values.

```java
List<ProjectionDetails> details = client.list().get();

for (ProjectionDetails detail: details) {
    System.out.println(
        detail.getName() + ", " +
        detail.getStatus() + ", " +
        detail.getCheckpointStatus() + ", " +
        detail.getMode() + ", " +
        detail.getProgress());
}
```

## Get status

Gets the status of a named projection.
See the [projection details](#projection-details) section for an explanation of the returned values.

```java
ProjectionDetails status = client.getStatus("$by_category").get();
System.out.println(
    status.getName() + ", " +
    status.getStatus() + ", " +
    status.getCheckpointStatus() + ", " +
    status.getMode() + ", " +
    status.getProgress());
```

## Get state

Retrieves the state of a projection.

```java
public static class CountResult {
    private int count;
    public int getCount() {
        return count;
    }
    public void setCount(final int count){
        this.count = count;
    }
}

String name = "get_state_example";
String js =
    "fromAll()" +
    ".when({" +
    "    $init() {" +
    "        return {" +
    "            count: 0," +
    "        };" +
    "    }," +
    "    $any(s, e) {" +
    "        s.count += 1;" +
    "    }" +
    "})" +
    ".outputState();";

client.create(name, js).get();

Thread.sleep(500); //give it some time to process and have a state.

CountResult result = client
    .getState(name, CountResult.class)
    .get();

System.out.println(result);
```

## Get result

Retrieves the result of the named projection and partition.

```java
String name = "get_result_example";
String js =
    "fromAll()" +
    ".when({" +
    "    $init() {" +
    "        return {" +
    "            count: 0," +
    "        };" +
    "    }," +
    "    $any(s, e) {" +
    "        s.count += 1;" +
    "    }" +
    "})" +
    ".transformBy((state) => state.count)" +
    ".outputState();";

client.create(name, js).get();

Thread.sleep(500); //give it some time to process and have a state.

int result = client
        .getResult(name, int.class)
        .get();

System.out.println(result);
```

## Projection Details

The `list`, and `getStatus` methods return detailed statistics and information
about projections. Below is an explanation of the fields included in the
projection details:

| Field                                | Description                                                                                                                                                                                           |
| ------------------------------------ | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `name`, `effectiveName`              | The name of the projection                                                                                                                                                                            |
| `status`                             | A human readable string of the current statuses of the projection (see below)                                                                                                                         |
| `stateReason`                        | A human readable string explaining the reason of the current projection state                                                                                                                         |
| `checkpointStatus`                   | A human readable string explaining the current operation performed on the checkpoint : `requested`, `writing`                                                                                         |
| `mode`                               | `Continuous`, `OneTime` , `Transient`                                                                                                                                                                 |
| `coreProcessingTime`                 | The total time, in ms, the projection took to handle events since the last restart                                                                                                                    |
| `progress`                           | The progress, in %, indicates how far this projection has processed event, in case of a restart this could be -1% or some number. It will be updated as soon as a new event is appended and processed |
| `writesInProgress`                   | The number of write requests to emitted streams currently in progress, these writes can be batches of events                                                                                          |
| `readsInProgress`                    | The number of read requests currently in progress                                                                                                                                                     |
| `partitionsCached`                   | The number of cached projection partitions                                                                                                                                                            |
| `position`                           | The Position of the last processed event                                                                                                                                                              |
| `lastCheckpoint`                     | The Position of the last checkpoint of this projection                                                                                                                                                |
| `eventsProcessedAfterRestart`        | The number of events processed since the last restart of this projection                                                                                                                              |
| `bufferedEvents`                     | The number of events in the projection read buffer                                                                                                                                                    |
| `writePendingEventsBeforeCheckpoint` | The number of events waiting to be appended to emitted streams before the pending checkpoint can be written                                                                                           |
| `writePendingEventsAfterCheckpoint`  | The number of events to be appended to emitted streams since the last checkpoint                                                                                                                      |
| `version`                            | This is used internally, the version is increased when the projection is edited or reset                                                                                                              |
| `epoch`                              | This is used internally, the epoch is increased when the projection is reset                                                                                                                          |

The `status` string is a combination of the following values.

The first 3 are the most common one, as the other one are transient values while
the projection is initialised or stopped

| Value              | Description                                                                                                             |
|--------------------|-------------------------------------------------------------------------------------------------------------------------|
| Running            | The projection is running and processing events                                                                         |
| Stopped            | The projection is stopped and is no longer processing new events                                                        |
| Faulted            | An error occurred in the projection, `StateReason` will give the fault details, the projection is not processing events |
| Initial            | This is the initial state, before the projection is fully initialised                                                   |
| Suspended          | The projection is suspended and will not process events, this happens while stopping the projection                     |
| LoadStateRequested | The state of the projection is being retrieved, this happens while the projection is starting                           |
| StateLoaded        | The state of the projection is loaded, this happens while the projection is starting                                    |
| Subscribed         | The projection has successfully subscribed to its readers, this happens while the projection is starting                |
| FaultedStopping    | This happens before the projection is stopped due to an error in the projection                                         |
| Stopping           | The projection is being stopped                                                                                         |
| CompletingPhase    | This happens while the projection is stopping                                                                           |
| PhaseCompleted     | This happens while the projection is stopping                                                                           |
