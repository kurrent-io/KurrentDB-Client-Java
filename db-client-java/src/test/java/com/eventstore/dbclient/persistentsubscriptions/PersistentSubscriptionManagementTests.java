package com.eventstore.dbclient.persistentsubscriptions;

import com.eventstore.dbclient.*;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SuppressWarnings("unchecked")
public interface PersistentSubscriptionManagementTests extends ConnectionAware {
    @Test
    @Order(1)
    default void testListPersistentSubscriptions() throws Throwable {
        EventStoreDBPersistentSubscriptionsClient client = getDefaultPersistentSubscriptionClient();
        String groupName = generateName();
        String streamA = generateName();
        String streamB = generateName();

        client.createToStream(streamA, groupName)
                .get();

        client.createToStream(streamB, groupName)
                .get();

        List<PersistentSubscriptionInfo> subs = client.listAll().get();

        int count = 0;
        for (PersistentSubscriptionInfo info: subs) {
            if (info.getEventSource().equals(streamA) || info.getEventSource().equals(streamB)) {
                count++;
            }
        }

        Assertions.assertEquals(2, count);
    }

    @Test
    @Order(2)
    default void testListPersistentSubscriptionsForStream() throws Throwable {
        Exceptions exceptions = new Exceptions().registerGoAwayError();

        EventStoreDBPersistentSubscriptionsClient client = getDefaultPersistentSubscriptionClient();
        String streamName = generateName();
        String groupName = generateName();

        flaky(10, exceptions, () -> client.createToStream(streamName, groupName).get());

        exceptions.register(ResourceNotFoundException.class);

        List<PersistentSubscriptionToStreamInfo> subs = flaky(10, exceptions, () -> client.listToStream(streamName).get());

        Assertions.assertEquals(subs.size(), 1);
        Assertions.assertEquals(subs.get(0).getEventSource(), streamName);
    }

    @Test
    @Order(3)
    default void testListPersistentSubscriptionsToAll() throws Throwable {
        EventStoreDBPersistentSubscriptionsClient client = getDefaultPersistentSubscriptionClient();
        String groupName = generateName();
        client.createToAll(groupName)
                .get();

        List<PersistentSubscriptionToAllInfo> subs = client.listToAll().get();

        Assertions.assertTrue(subs.size() >= 1);

        boolean found = false;

        for (PersistentSubscriptionToAllInfo info : subs) {
            if (info.getEventSource().equals("$all") && info.getGroupName().equals(groupName)) {
                found = true;
                break;
            }
        }

        Assertions.assertTrue(found);
    }

    @Test
    @Order(4)
    default void testGetPersistentSubscriptionInfo() throws Throwable {
        EventStoreDBPersistentSubscriptionsClient client = getDefaultPersistentSubscriptionClient();
        String streamName = generateName();
        String groupName = generateName();
        client.createToStream(streamName, groupName)
                .get();
        Optional<PersistentSubscriptionToStreamInfo> result = Optional.empty();
        for (int i = 0; i < 10; i++) {
            result = client.getInfoToStream(streamName, groupName).get();
            if (!result.isPresent()) {
                Thread.sleep(1000);
                continue;
            }

            break;
        }

        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals(streamName, result.get().getEventSource());
    }

    @Test
    @Order(5)
    default void testGetPersistentSubscriptionInfoToAll() throws Throwable {
        EventStoreDBPersistentSubscriptionsClient client = getDefaultPersistentSubscriptionClient();
        String groupName = generateName();

        client.createToAll(groupName)
                .get();

        Optional<PersistentSubscriptionToAllInfo> result = client.getInfoToAll(groupName).get();

        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals("$all", result.get().getEventSource());
        Assertions.assertEquals(result.get().getGroupName(), groupName);
    }

    @Test
    @Order(6)
     default void testGetPersistentSubscriptionInfoNotExisting() throws Throwable {
        EventStoreDBPersistentSubscriptionsClient client = getDefaultPersistentSubscriptionClient();
        Optional<PersistentSubscriptionToStreamInfo> result = client.getInfoToStream(generateName(), generateName()).get();

        Assertions.assertFalse(result.isPresent());
    }

    @Test
    @Order(7)
    @Timeout(value = 2, unit = TimeUnit.MINUTES)
    default void testReplayParkedMessages() throws Throwable {
        Exceptions exceptions = new Exceptions().registerGoAwayError();
        EventStoreDBPersistentSubscriptionsClient client = getDefaultPersistentSubscriptionClient();
        final KurrentDBClient streamClient = getDatabase().defaultClient();
        final JsonMapper jsonMapper = new JsonMapper();
        final String streamName = generateName();
        final String groupName = generateName();

        flaky(10, exceptions, () -> client.createToStream(streamName, groupName).get());

        final CompletableFuture allNacked = new CompletableFuture();
        final CompletableFuture allReplayed = new CompletableFuture();

        client.subscribeToStream(streamName, groupName, new PersistentSubscriptionListener() {
            int count = 0;
            @Override
            public void onEvent(PersistentSubscription subscription, int retryCount, ResolvedEvent event) {
                if (count < 2)
                    subscription.nack(NackAction.Park, "because reason", event);
                else
                    subscription.ack(event);

                count++;

                if (count == 2) {
                    allNacked.complete(null);
                }

                if (count >= 4) {
                    allReplayed.complete(null);
                    subscription.stop();
                }
            }
        }).get();

        for (int i = 0; i < 2; i++) {
            EventData data = EventData.builderAsJson("foobar", jsonMapper.writeValueAsBytes(new Foo())).build();
            streamClient.appendToStream(streamName, data).get();
        }

        exceptions.register(ResourceNotFoundException.class);

        CompletableFuture.runAsync(() -> {
            try {
                allNacked.get();
                // We give the server some time to park those events.
                Thread.sleep(10_000);
                flaky(10, exceptions, () -> client.replayParkedMessagesToStream(streamName, groupName).get());
                allReplayed.get();
            } catch (Exception e) {
                getLogger().error("Error when running testReplayParkedMessages", e);
                throw new RuntimeException(e);
            }
        }).get();
    }

    @Test
    @Order(8)
    @Timeout(value = 2, unit = TimeUnit.MINUTES)
    default void testReplayParkedMessagesToAll() throws Throwable {
        Exceptions exceptions = new Exceptions().registerGoAwayError();
        EventStoreDBPersistentSubscriptionsClient client = getDefaultPersistentSubscriptionClient();
        final KurrentDBClient streamClient = getDatabase().defaultClient();
        final JsonMapper jsonMapper = new JsonMapper();
        String streamName = generateName();
        String groupName = generateName();

        flaky(10, exceptions, () -> client.createToAll(groupName).get());

        final CompletableFuture allNacked = new CompletableFuture();
        final CompletableFuture allReplayed = new CompletableFuture();

        client.subscribeToAll(groupName, new PersistentSubscriptionListener() {
            int count = 0;
            @Override
            public void onEvent(PersistentSubscription subscription, int retryCount, ResolvedEvent event) {
                if (count < 2 && event.getOriginalEvent().getStreamId().equals(streamName))
                    subscription.nack(NackAction.Park, "because reason", event);
                else
                    subscription.ack(event);

                if (event.getOriginalEvent().getStreamId().equals(streamName) || event.getEvent().getStreamId().equals(streamName))
                    count++;

                if (count == 2) {
                    allNacked.complete(null);
                }

                if (count >= 4) {
                    allReplayed.complete(null);
                    subscription.stop();
                }
            }
        }).get();

        for (int i = 0; i < 2; i++) {
            EventData data = EventData.builderAsJson("foobar", jsonMapper.writeValueAsBytes(new Foo())).build();
            streamClient.appendToStream(streamName, data).get();
        }

        exceptions.register(ResourceNotFoundException.class);
        CompletableFuture.runAsync(() -> {
            try {
                allNacked.get();
                // We give the server some time to park those events.
                Thread.sleep(10_000);
                flaky(10, exceptions, () -> client.replayParkedMessagesToAll(groupName).get());
                allReplayed.get();
            } catch (Exception e) {
                getLogger().error("Error when running testReplayParkedMessages", e);
                throw new RuntimeException(e);
            }
        }).get();
    }

    @Test
    @Order(9)
    default void testEncoding() throws Throwable {
        Exceptions exceptions = new Exceptions().registerGoAwayError();
        EventStoreDBPersistentSubscriptionsClient client = getDefaultPersistentSubscriptionClient();
        String streamName = String.format("/foo/%s/stream", generateName());
        String groupName = String.format("/foo/%s/group", generateName());

        flaky(10, exceptions, () -> client.createToStream(streamName, groupName).get());
        Optional<PersistentSubscriptionToStreamInfo> info = Optional.empty();
        for (int i = 0; i < 10; i++) {
            info = client.getInfoToStream(streamName, groupName).get();

            if (!info.isPresent()) {
                Thread.sleep(500);
                continue;
            }

            break;
        }

       Assertions.assertTrue(info.isPresent());
       Assertions.assertEquals(info.get().getEventSource(), streamName);
       Assertions.assertEquals(info.get().getGroupName(), groupName);
    }

    @Test
    @Order(10)
    default void testRestartSubsystem() throws Throwable {
        EventStoreDBPersistentSubscriptionsClient client = getDefaultPersistentSubscriptionClient();
        client.restartSubsystem().get();
    }
}
