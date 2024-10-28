package com.eventstore.dbclient.streams;

import com.eventstore.dbclient.*;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.UUID;

public interface AppendTests extends ConnectionAware {
    @Test
    default void testAppendMultipleEvents() throws Throwable {
        EventStoreDBClient client = getDatabase().defaultClient();

        final String streamName = generateName();
        final String eventType = "TestEvent";
        final String eventId1 = UUID.randomUUID().toString();
        final String eventId2 = UUID.randomUUID().toString();
        final byte[] eventMetaData = new byte[]{0xd, 0xe, 0xa, 0xd};
        final JsonMapper jsonMapper = new JsonMapper();

        // Create first event data with metadata bytes
        EventData event1 = EventData.builderAsJson(eventType, jsonMapper.writeValueAsBytes(new Foo()))
                .metadataAsBytes(eventMetaData)
                .eventId(UUID.fromString(eventId1))
                .build();

        // Create second event data with JSON metadata
        EventData event2 = EventData.builderAsJson(eventType, jsonMapper.writeValueAsBytes(new Foo()))
                .metadataAsBytes(jsonMapper.writeValueAsBytes(new Foo()))
                .eventId(UUID.fromString(eventId2))
                .build();

        AppendToStreamOptions appendOptions = AppendToStreamOptions.get()
                .expectedRevision(ExpectedRevision.noStream());

        // Append both events to stream
        WriteResult appendResult = client.appendToStream(streamName, appendOptions, event1, event2)
                .get();

        // Validate the append operation
        Assertions.assertEquals(ExpectedRevision.expectedRevision(1), appendResult.getNextExpectedRevision());

        ReadStreamOptions readStreamOptions = ReadStreamOptions.get()
                .fromEnd()
                .backwards()
                .maxCount(2);

        // Ensure both appended events are readable
        ReadResult result = client.readStream(streamName, readStreamOptions)
                .get();

        Assertions.assertEquals(2, result.getEvents().size());
        RecordedEvent first = result.getEvents().get(1).getEvent();
        RecordedEvent second = result.getEvents().get(0).getEvent();
        JsonMapper mapper = new JsonMapper();

        // Verify first event details
        Assertions.assertEquals(streamName, first.getStreamId());
        Assertions.assertEquals(eventType, first.getEventType());
        Assertions.assertEquals(eventId1, first.getEventId().toString());
        Assertions.assertArrayEquals(eventMetaData, first.getUserMetadata());
        Assertions.assertEquals(new Foo(), mapper.readValue(first.getEventData(), Foo.class));

        // Verify second event details
        Assertions.assertEquals(streamName, second.getStreamId());
        Assertions.assertEquals(eventType, second.getEventType());
        Assertions.assertEquals(eventId2, second.getEventId().toString());
        Assertions.assertEquals(new Foo(), mapper.readValue(second.getEventData(), Foo.class));
    }
}

