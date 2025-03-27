package io.kurrent.dbclient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;

import java.util.UUID;

/**
 * Utility class to help building an <i>EventData</i>.
 */
public class EventDataBuilder {
    private byte[] eventData;
    private byte[] metadata;
    private String eventType;
    private boolean isJson;
    private UUID id;

    EventDataBuilder(){}

    /**
     * Configures an event data builder to host a JSON payload.
     * @param eventType event's type.
     * @param eventData event's payload.
     * @return an event data builder.
     */
    public static EventDataBuilder json(String eventType, byte[] eventData) {
        return json(null, eventType, eventData);
    }

    /**
     * Configures an event data builder to host a JSON payload.
     * @param id event's id.
     * @param eventType event's type.
     * @param eventData event's payload.
     * @return an event data builder.
     */
    public static EventDataBuilder json(UUID id, String eventType, byte[] eventData) {
        return binary(id, eventType, eventData, true);
    }

    /**
     * Configures an event data builder to host a binary payload.
     * @param eventType event's type.
     * @param eventData event's payload.
     * @return an event data builder.
     */
    public static EventDataBuilder binary(String eventType, byte[] eventData) {
        return binary(null, eventType, eventData);
    }

    /**
     * Configures an event data builder to host a binary payload.
     * @param id event's id.
     * @param eventType event's type.
     * @param eventData event's payload.
     * @return an event data builder.
     */
    public static EventDataBuilder binary(UUID id, String eventType, byte[] eventData) {
        return binary(id, eventType, eventData, false);
    }

    /**
     * Configures an event data builder to host a binary payload.
     * @param id event's id.
     * @param eventType event's type.
     * @param eventData event's payload.
     * @param isJson whether the payload is JSON or not.
     * @return an event data builder.
     */
    public static EventDataBuilder binary(UUID id, String eventType, byte[] eventData, boolean isJson) {
        EventDataBuilder self = new EventDataBuilder();

        self.eventData = eventData;
        self.eventType = eventType;
        self.isJson = isJson;
        self.id = id;

        return self;
    }

    /**
     * Sets event's unique identifier.
     */
    public EventDataBuilder eventId(UUID id) {
        this.id = id;
        return this;
    }

    /**
     * Sets event's custom user metadata.
     */
    public EventDataBuilder metadataAsBytes(byte[] value) {
        this.metadata = value;
        return this;
    }

    /**
     * Builds an event ready to be sent to EventStoreDB.
     *
     * @see EventData
     */
    public EventData build() {
        UUID eventId = this.id == null ? UUID.randomUUID() : this.id;
        String contentType = this.isJson ? ContentType.JSON : ContentType.BYTES;
        return new EventData(eventId, this.eventType, contentType, this.eventData, this.metadata);
    }
}
