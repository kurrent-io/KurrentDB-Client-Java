package io.kurrent.dbclient.streams;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.kurrent.dbclient.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.UUID;

public interface AppendTests extends ConnectionAware {
    @Test
    default void testAppendSingleEventNoStream() throws Throwable {
        KurrentDBClient client = getDatabase().defaultClient();
        String streamName = generateName();
        UUID eventId = UUID.randomUUID();
        Foo foo = new Foo();
        byte[] fooBytes = mapper.writeValueAsBytes(foo);

        EventData event = EventData.builderAsJson("TestEvent", fooBytes)
                .metadataAsBytes(fooBytes)
                .eventId(eventId)
                .build();

        WriteResult appendResult = client.appendToStream(
                streamName, AppendToStreamOptions.get().streamState(StreamState.noStream()), event).get();

        Assertions.assertEquals(StreamState.streamRevision(0), appendResult.getNextExpectedRevision());

        ReadResult result = client.readStream(streamName, ReadStreamOptions.get().fromEnd().backwards().maxCount(1)).get();
        RecordedEvent first = result.getEvents().get(0).getEvent();
        ObjectNode userMetadata = mapper.readValue(first.getUserMetadata(), ObjectNode.class);

        Assertions.assertAll(
                () -> Assertions.assertEquals(streamName, first.getStreamId()),
                () -> Assertions.assertEquals("TestEvent", first.getEventType()),
                () -> Assertions.assertEquals(eventId.toString(), first.getEventId().toString()),
                () -> Assertions.assertEquals(foo, mapper.readValue(first.getEventData(), Foo.class)),
                () -> Assertions.assertEquals(foo, mapper.readValue(first.getUserMetadata(), Foo.class)),
                () -> Assertions.assertFalse(userMetadata.has(ClientTelemetryConstants.Metadata.TRACE_ID)),
                () -> Assertions.assertFalse(userMetadata.has(ClientTelemetryConstants.Metadata.SPAN_ID))
        );
    }

    @Test
    default void testAppendMultipleEventsAtOnce() throws Throwable {
        KurrentDBClient client = getDatabase().defaultClient();
        String streamName = generateName();
        int eventCount = 5;

        WriteResult result = client.appendToStream(streamName,
                AppendToStreamOptions.get().streamState(StreamState.noStream()),
                generateEvents(eventCount, "TestEvent").iterator()).get();

        Assertions.assertEquals(StreamState.streamRevision(eventCount - 1), result.getNextExpectedRevision());
        Assertions.assertEquals(eventCount, client.readStream(streamName, ReadStreamOptions.get()).get().getEvents().size());
    }

    @Test
    default void testStreamStateOptimisticConcurrency() throws Throwable {
        KurrentDBClient client = getDatabase().defaultClient();

        String anyStream = generateName();
        appendEvent(client, anyStream, StreamState.any());
        appendEvent(client, anyStream, StreamState.any());
        Assertions.assertEquals(2, client.readStream(anyStream, ReadStreamOptions.get()).get().getEvents().size());

        String existsStream = generateName();
        assertWrongExpectedVersion(client, existsStream, StreamState.streamExists(), StreamState.streamExists(), StreamState.noStream());
        appendEvent(client, existsStream, StreamState.noStream());
        appendEvent(client, existsStream, StreamState.streamExists());

        String noStream = generateName();
        appendEvent(client, noStream, StreamState.noStream());
        assertWrongExpectedVersion(client, noStream, StreamState.noStream(), StreamState.noStream(), StreamState.streamRevision(0));

        String revStream = generateName();
        appendEvent(client, revStream, StreamState.noStream());
        appendEvent(client, revStream, StreamState.streamRevision(0));
        assertWrongExpectedVersion(client, revStream, StreamState.streamRevision(99), StreamState.streamRevision(99), StreamState.streamRevision(1));
    }

    default EventData createTestEvent() throws Exception {
        return EventData.builderAsJson("TestEvent", mapper.writeValueAsBytes(new Foo()))
                .eventId(UUID.randomUUID())
                .build();
    }

    default void appendEvent(KurrentDBClient client, String streamName, StreamState state) throws Exception {
        client.appendToStream(streamName, AppendToStreamOptions.get().streamState(state), createTestEvent()).get();
    }

    default void assertWrongExpectedVersion(KurrentDBClient client, String streamName, StreamState state, StreamState expectedState, StreamState actualState) {
        WrongExpectedVersionException ex = Assertions.assertThrows(WrongExpectedVersionException.class, () -> {
            try {
                appendEvent(client, streamName, state);
            } catch (java.util.concurrent.ExecutionException e) {
                if (e.getCause() != null) {
                    throw e.getCause();
                }
                throw e;
            }
        });
        Assertions.assertEquals(streamName, ex.getStreamName());
        Assertions.assertEquals(expectedState, ex.getExpectedState());
        Assertions.assertEquals(actualState, ex.getActualState());
    }

}
