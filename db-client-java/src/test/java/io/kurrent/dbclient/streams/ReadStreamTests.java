package io.kurrent.dbclient.streams;

import io.kurrent.dbclient.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public interface ReadStreamTests extends ConnectionAware {
    @Test
    default void testReadStreamEvents() throws Throwable {
        String streamName = generateName();
        int count = 3;
        List<BazEvent> expecteds = generateBazEvent(count);
        List<BazEvent> actuals = new ArrayList<>();
        List<EventData> events = new ArrayList<>();

        for (BazEvent event : expecteds) {
            events.add(serializeBazEvent(event));
        }

        KurrentDBClient client = getDefaultClient();

        client.appendToStream(streamName, events.iterator()).get();
        ReadResult result = client.readStream(streamName, ReadStreamOptions.get()).get();

        for (ResolvedEvent resolvedEvent : result.getEvents()) {
            BazEvent event = deserializeBazEvent(resolvedEvent.getOriginalEvent().getEventData());
            actuals.add(event);
        }

        for (int i = 0; i < count; i++) {
            BazEvent actual = actuals.get(i);
            BazEvent expected = expecteds.get(i);
            Assertions.assertEquals(expected.getName(), actual.getName());
            Assertions.assertEquals(expected.getAge(), actual.getAge());
        }
    }

    @Test
    default void testNonexistentStream() throws Throwable {
        String streamName = generateName();

        Assertions.assertThrows(StreamNotFoundException.class, () -> {
            try {
                getDefaultClient().readStream(streamName, ReadStreamOptions.get()).get();
            } catch (ExecutionException e) {
                throw e.getCause();
            }
        });
    }
}
