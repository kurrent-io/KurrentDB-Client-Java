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
        String eventType = "TestEvent";
        UUID eventId = UUID.randomUUID();
        Foo foo = new Foo();
        byte[] fooBytes = mapper.writeValueAsBytes(foo);

        EventData event = EventData.builderAsJson(eventType, fooBytes)
                .metadataAsBytes(fooBytes)
                .eventId(eventId)
                .build();

        WriteResult appendResult = client.appendToStream(
                streamName,
                AppendToStreamOptions.get().streamState(StreamState.noStream()),
                event
        ).get();

        Assertions.assertEquals(StreamState.streamRevision(0), appendResult.getNextExpectedRevision());

        ReadResult result = client.readStream(
                streamName,
                ReadStreamOptions.get().fromEnd().backwards().maxCount(1)
        ).get();

        Assertions.assertEquals(1, result.getEvents().size());
        RecordedEvent first = result.getEvents().get(0).getEvent();
        ObjectNode userMetadata = mapper.readValue(first.getUserMetadata(), ObjectNode.class);

        Assertions.assertAll(
                () -> Assertions.assertEquals(streamName, first.getStreamId()),
                () -> Assertions.assertEquals(eventType, first.getEventType()),
                () -> Assertions.assertEquals(eventId.toString(), first.getEventId().toString()),
                () -> Assertions.assertEquals(foo, mapper.readValue(first.getEventData(), Foo.class)),
                () -> Assertions.assertEquals(foo, mapper.readValue(first.getUserMetadata(), Foo.class)),
                () -> Assertions.assertFalse(userMetadata.has(ClientTelemetryConstants.Metadata.TRACE_ID)),
                () -> Assertions.assertFalse(userMetadata.has(ClientTelemetryConstants.Metadata.SPAN_ID))
        );
    }
}
