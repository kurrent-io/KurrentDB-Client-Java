package io.kurrent.dbclient.persistentsubscriptions;

import io.kurrent.dbclient.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;

public interface PersistentSubscriptionToAllWithFilterTests extends ConnectionAware {
    @Test
    default void testPersistentSubscriptionToAllWithFilter() throws Throwable {
        Exceptions exceptions = new Exceptions().registerGoAwayError();
        KurrentDBPersistentSubscriptionsClient client = getDefaultPersistentSubscriptionClient();
        KurrentDBClient streamsClient = getDatabase().defaultClient();
        String groupName = generateName();
        int filteredEventTypeCount = 10;
        String filteredEventType = "filtered-event-type";
        String otherEventType = UUID.randomUUID().toString();

        flaky(10, exceptions, () -> streamsClient.appendToStream(generateName(), generateEvents(5, otherEventType).iterator()).get());
        flaky(10, exceptions, () -> streamsClient.appendToStream(generateName(), generateEvents(filteredEventTypeCount, filteredEventType).iterator()).get());
        flaky(10, exceptions, () -> streamsClient.appendToStream(generateName(), generateEvents(10, otherEventType).iterator()).get());

        final CountDownLatch receivedEvents = new CountDownLatch(filteredEventTypeCount);
        final CountDownLatch cancellation = new CountDownLatch(1);

        PersistentSubscriptionListener listener = new PersistentSubscriptionListener() {
            int current = 0;

            @Override
            public void onEvent(PersistentSubscription subscription, int retryCount, ResolvedEvent event) {
                RecordedEvent record = event.getEvent();

                Assertions.assertEquals(filteredEventType, record.getEventType());

                receivedEvents.countDown();
                current++;
            }

            @Override
            public void onCancelled(PersistentSubscription subscription, Throwable throwable) {
                if (throwable == null) {
                    cancellation.countDown();
                    return;
                }

                Assertions.fail(throwable.getMessage());
            }
        };

        SubscriptionFilter filter = SubscriptionFilter.newBuilder()
            .addEventTypePrefix(filteredEventType)
            .build();

        flaky(10, exceptions, () ->
                client.createToAll(
                        groupName,
                        CreatePersistentSubscriptionToAllOptions.get().fromStart().filter(filter))
                .get());

        PersistentSubscription result = client.subscribeToAll(groupName, listener)
            .get();

        Assertions.assertNotNull(result.getSubscriptionId());
        Assertions.assertNotEquals("", result.getSubscriptionId());

        receivedEvents.await();
        result.stop();
        cancellation.await();
    }
}
