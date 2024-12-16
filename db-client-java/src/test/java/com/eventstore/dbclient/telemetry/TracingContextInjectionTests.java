package com.eventstore.dbclient.telemetry;

import com.eventstore.dbclient.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public interface TracingContextInjectionTests extends TelemetryAware {
    @Test
    @Timeout(value = 2, unit = TimeUnit.MINUTES)
    default void testTracingContextInjectionDoesNotAffectEventBody() throws Throwable {
        KurrentDBClient streamsClient = getDefaultClient();
        KurrentDBPersistentSubscriptionsClient psClient = getDefaultPersistentSubscriptionClient();

        String streamName = generateName();
        String groupName = "aGroup";

        EventData[] events = {
                EventData.builderAsJson("JsonEvent", mapper.writeValueAsBytes(new Foo()))
                        .eventId(UUID.randomUUID())
                        .build(),
                EventData.builderAsBinary("ProtoEvent", mapper.writeValueAsBytes(new Foo()))
                        .eventId(UUID.randomUUID())
                        .build()
        };

        Exceptions exceptions = new Exceptions().registerGoAwayError();
        flaky(10, exceptions, () -> psClient.createToStream(streamName, groupName).get());

        streamsClient.appendToStream(streamName, events).get();

        CountDownLatch subscribeSpansLatch = new CountDownLatch(events.length);
        onOperationSpanEnded(ClientTelemetryConstants.Operations.SUBSCRIBE, span -> subscribeSpansLatch.countDown());

        ArrayList<RecordedEvent> receivedEvents = new ArrayList<>();
        PersistentSubscription subscription = psClient.subscribeToStream(
                streamName,
                groupName,
                SubscribePersistentSubscriptionOptions.get().bufferSize(32),
                new PersistentSubscriptionListener() {
                    @Override
                    public void onEvent(PersistentSubscription subscription, int retryCount, ResolvedEvent event) {
                        receivedEvents.add(event.getEvent());
                    }
                }
        ).get();

        subscribeSpansLatch.await();
        subscription.stop();

        for (RecordedEvent receivedEvent : receivedEvents) {
            EventData sentEvent = Arrays.stream(events)
                    .filter(e -> e.getEventId().equals(receivedEvent.getEventId()))
                    .findFirst()
                    .orElse(null);

            Assertions.assertNotNull(sentEvent);
            Assertions.assertArrayEquals(sentEvent.getEventData(), receivedEvent.getEventData());
            Assertions.assertEquals(sentEvent.getContentType(), receivedEvent.getContentType());
        }
    }
}
