package com.eventstore.dbclient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.github.javafaker.Faker;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public interface ConnectionAware {
    Faker faker = new Faker();
    JsonMapper mapper = new JsonMapper();

    Database getDatabase();

    Logger getLogger();

    default KurrentDBClient getDefaultClient() {
        return getDatabase().defaultClient();
    }

    default KurrentDBProjectionManagementClient getDefaultProjectionClient() {
        return KurrentDBProjectionManagementClient.from(getDefaultClient());
    }

    default KurrentDBPersistentSubscriptionsClient getDefaultPersistentSubscriptionClient() {
        return KurrentDBPersistentSubscriptionsClient.from(getDefaultClient());
    }

    default String generateName() {
        return String.format("%s-%s", faker.pokemon().name(), faker.pokemon().name());
    }

    default ArrayList<EventData> generateEvents(int amount, String eventType) {
        ArrayList<EventData> events = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            events.add(EventDataBuilder.json(UUID.randomUUID(), eventType, new byte[]{}).build());
        }

        return events;
    }

    default ArrayList<BazEvent> generateBazEvent(int count) {
        ArrayList<BazEvent> events = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            BazEvent event = new BazEvent(generateName(), faker.number().randomDigit());
            events.add(event);
        }

        return events;
    }

    default EventData serializeBazEvent(BazEvent event) throws JsonProcessingException {
        return serializeBazEvent(event, null);
    }

    default EventData serializeBazEvent(BazEvent event, String eventType) throws JsonProcessingException {
        String type = eventType == null ? generateName() : eventType;
        return EventDataBuilder.json(type, mapper.writeValueAsBytes(event)).build();
    }

    default ArrayList<EventData> serializeBazEvents(Collection<BazEvent> events) throws JsonProcessingException {
        return serializeBazEvents(events, null);
    }

    default ArrayList<EventData> serializeBazEvents(Collection<BazEvent> events, String eventStype) throws JsonProcessingException {
        ArrayList<EventData> data = new ArrayList<>();

        for(BazEvent event : events) {
            data.add(serializeBazEvent(event, eventStype));
        }

        return data;
    }

    default BazEvent deserializeBazEvent(byte[] data) throws IOException {
        return mapper.readValue(data, BazEvent.class);
    }

    default <A> A flaky(int retries, Exceptions exceptions, Action<A> action) throws Exception {
        Throwable last = null;

        for (int i = 0; i < retries; i++) {
            try {
                return action.run();
            } catch (Exception e) {
                if (e instanceof ExecutionException) {
                    ExecutionException ex = (ExecutionException) e;
                    if (exceptions.contains(ex.getCause())) {
                        last = ex.getCause();
                        Thread.sleep(500);
                    }
                    else
                        throw e;
                } else {
                    throw e;
                }
            }
        }

        if (last != null)
            throw new Exception(last);
        else
            throw new IllegalStateException("flaky method entered an illegal state");
    }
}
