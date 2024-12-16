package io.kurrent.dbclient.streams;

import io.kurrent.dbclient.*;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.UUID;

public interface AppendTests extends ConnectionAware {
    @Test
    default void testAppendSingleEventNoStream() throws Throwable {
        KurrentDBClient client = getDatabase().defaultClient();

        final String streamName = generateName();
        final String eventType = "TestEvent";
        final String eventId = "38fffbc2-339e-11ea-8c7b-784f43837872";
        final byte[] eventMetaData = new byte[]{0xd, 0xe, 0xa, 0xd};
        final JsonMapper jsonMapper = new JsonMapper();

        EventData event = EventData.builderAsJson(eventType, jsonMapper.writeValueAsBytes(new Foo()))
                .metadataAsBytes(eventMetaData)
                .eventId(UUID.fromString(eventId))
                .build();

        AppendToStreamOptions appendOptions = AppendToStreamOptions.get()
                .expectedRevision(ExpectedRevision.noStream());

        WriteResult appendResult = client.appendToStream(streamName, appendOptions, event)
                .get();

        Assertions.assertEquals(ExpectedRevision.expectedRevision(0), appendResult.getNextExpectedRevision());

        ReadStreamOptions readStreamOptions = ReadStreamOptions.get()
                .fromEnd()
                .backwards()
                .maxCount(1);

        // Ensure appended event is readable
        ReadResult result = client.readStream(streamName, readStreamOptions)
                .get();

        Assertions.assertEquals(1, result.getEvents().size());
        RecordedEvent first = result.getEvents().get(0).getEvent();
        JsonMapper mapper = new JsonMapper();

        Assertions.assertEquals(streamName, first.getStreamId());
        Assertions.assertEquals(eventType, first.getEventType());
        Assertions.assertEquals(eventId, first.getEventId().toString());
        Assertions.assertArrayEquals(eventMetaData, first.getUserMetadata());
        Assertions.assertEquals(new Foo(), mapper.readValue(first.getEventData(), Foo.class));
    }
}
