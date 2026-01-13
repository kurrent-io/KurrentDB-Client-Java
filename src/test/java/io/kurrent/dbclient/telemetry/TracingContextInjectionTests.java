package io.kurrent.dbclient.telemetry;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.kurrent.dbclient.*;
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

    @Test
    @Timeout(value = 2, unit = TimeUnit.MINUTES)
    default void testExistingTracingContextIsPreserved() throws Throwable {
        KurrentDBClient client = getDefaultClient();

        String streamName = generateName();
        String existingTraceId = "0af7651916cd43dd8448eb211c80319c";
        String existingSpanId = "b7ad6b7169203331";

        ObjectNode metadata = mapper.createObjectNode();
        metadata.put(ClientTelemetryConstants.Metadata.TRACE_ID, existingTraceId);
        metadata.put(ClientTelemetryConstants.Metadata.SPAN_ID, existingSpanId);

        EventData event = EventData.builderAsJson("TestEvent", mapper.writeValueAsBytes(new Foo()))
                .metadataAsBytes(mapper.writeValueAsBytes(metadata))
                .eventId(UUID.randomUUID())
                .build();

        client.appendToStream(streamName, event).get();

        ReadResult result = client.readStream(streamName, ReadStreamOptions.get()).get();

        Assertions.assertEquals(1, result.getEvents().size());
        RecordedEvent recordedEvent = result.getEvents().get(0).getEvent();
        ObjectNode recordedMetadata = mapper.readValue(recordedEvent.getUserMetadata(), ObjectNode.class);

        Assertions.assertAll(
                () -> Assertions.assertEquals(existingTraceId, recordedMetadata.get(ClientTelemetryConstants.Metadata.TRACE_ID).asText()),
                () -> Assertions.assertEquals(existingSpanId, recordedMetadata.get(ClientTelemetryConstants.Metadata.SPAN_ID).asText())
        );
    }
}
