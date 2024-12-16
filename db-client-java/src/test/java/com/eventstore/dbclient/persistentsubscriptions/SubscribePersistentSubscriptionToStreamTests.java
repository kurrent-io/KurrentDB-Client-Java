package com.eventstore.dbclient.persistentsubscriptions;

import com.eventstore.dbclient.*;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;

public interface SubscribePersistentSubscriptionToStreamTests extends ConnectionAware {
    @Test
    default void testSubscribePersistentSub() throws Throwable {
        Exceptions exceptions = new Exceptions().registerGoAwayError();
        String streamName = generateName();
        EventStoreDBPersistentSubscriptionsClient client = getDefaultPersistentSubscriptionClient();
        KurrentDBClient streamsClient = getDatabase().defaultClient();
        JsonMapper jsonMapper = new JsonMapper();

        flaky(10, exceptions, () -> client.createToStream(streamName, "aGroup")
                .get());

        EventDataBuilder builder = EventData.builderAsJson("foobar", jsonMapper.writeValueAsBytes(new Foo()));

        streamsClient.appendToStream(streamName, builder.build(), builder.build(), builder.build())
                .get();

        final CompletableFuture<Integer> result = new CompletableFuture<>();

        SubscribePersistentSubscriptionOptions connectOptions = SubscribePersistentSubscriptionOptions.get()
                .bufferSize(32);

        client.subscribeToStream(streamName, "aGroup", connectOptions, new PersistentSubscriptionListener() {
            private int count = 0;

            @Override
            public void onEvent(PersistentSubscription subscription, int retryCount, ResolvedEvent event) {
                ++this.count;

                subscription.ack(event);

                if (this.count == 6) {
                    result.complete(this.count);
                    subscription.stop();
                }
            }

            @Override
            public void onCancelled(PersistentSubscription subscription, Throwable throwable) {
                if (throwable == null) {
                    result.complete(count);
                    return;
                }

                result.completeExceptionally(throwable);
            }
        }).get();

        streamsClient.appendToStream(streamName, builder.build(), builder.build(), builder.build())
                .get();

        Assertions.assertEquals(6, result.get().intValue());
    }

    @Test
    default void testSubscribePersistentSubToAll() throws Throwable {
        Exceptions exceptions = new Exceptions().registerGoAwayError();
        String streamName = generateName();
        EventStoreDBPersistentSubscriptionsClient client = getDefaultPersistentSubscriptionClient();
        KurrentDBClient streamsClient = getDatabase().defaultClient();
        final JsonMapper jsonMapper = new JsonMapper();

        flaky(10, exceptions, () -> client.createToAll("aGroup")
                .get());

        EventDataBuilder builder = EventData.builderAsJson("foobar", jsonMapper.writeValueAsBytes(new Foo()));

        streamsClient.appendToStream(streamName, builder.build(), builder.build(), builder.build())
                .get();

        final CompletableFuture<Integer> result = new CompletableFuture<>();

        SubscribePersistentSubscriptionOptions connectOptions = SubscribePersistentSubscriptionOptions.get()
                .bufferSize(32);

        client.subscribeToAll("aGroup", connectOptions, new PersistentSubscriptionListener() {
            private int count = 0;

            @Override
            public void onEvent(PersistentSubscription subscription, int retryCount, ResolvedEvent resolvedEvent) {
                RecordedEvent event = resolvedEvent.getEvent();

                subscription.ack(resolvedEvent);

                if (!event.getEventType().equals("foobar"))
                    return;

                ++this.count;

                if (this.count == 6) {
                    result.complete(this.count);
                    subscription.stop();
                }
            }

            @Override
            public void onCancelled(PersistentSubscription subscription, Throwable throwable) {
                if (throwable == null) {
                    result.complete(count);
                    return;
                }

                result.completeExceptionally(throwable);
            }
        }).get();

        streamsClient.appendToStream(streamName, builder.build(), builder.build(), builder.build())
                .get();

        Assertions.assertEquals(6, result.get().intValue());
    }
}
