package io.kurrent.dbclient.misc;

import io.kurrent.dbclient.EventData;
import io.kurrent.dbclient.EventDataBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.UUID;

public class EventDataTests {
    @Test
    public void testBinaryConstructorWithId() throws Throwable {
        UUID id = UUID.randomUUID();

        EventData data = EventDataBuilder.binary(id, "type", new byte[]{}).build();

        Assertions.assertEquals(id, data.getEventId());
    }

    @Test
    public void testJsonConstructorWithId() throws Throwable {
        UUID id = UUID.randomUUID();

        EventData data = EventDataBuilder.json(id, "type", new byte[]{}).build();

        Assertions.assertEquals(id, data.getEventId());
    }
}
