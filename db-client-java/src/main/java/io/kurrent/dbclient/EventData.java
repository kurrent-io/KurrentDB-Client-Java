package io.kurrent.dbclient;

import java.util.UUID;

/**
 * Represents an event that will be sent to KurrentDB.
 */
public final class EventData {
    private final UUID eventId;
    private final String eventType;
    private final String contentType;
    private final byte[] eventData;
    private final byte[] userMetadata;

    EventData(UUID eventId, String eventType, String contentType, byte[] eventData, byte[] userMetadata) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.contentType = contentType;
        this.eventData = eventData;
        this.userMetadata = userMetadata;
    }

    /**
     * Returns event's unique identifier
     */
    public UUID getEventId() {
        return eventId;
    }

    /**
     * Returns event's type.
     */
    public String getEventType() {
        return eventType;
    }

    /**
     * Returns event's content's type
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Returns event's payload data
     */
    public byte[] getEventData() {
        return eventData;
    }

    /**
     * Returns event's custom user metadata.
     */
    public byte[] getUserMetadata() {
        return userMetadata;
    }

    /**
     * Configures an event data builder to host a JSON payload.
     * @param eventType event's type.
     * @param eventData event's payload.
     * @return an event data builder.
     * @param <A> a type that can be serialized in JSON.
     */
    @Deprecated
    public static <A> EventDataBuilder builderAsJson(String eventType, A eventData) {
        return builderAsJson(null, eventType, eventData);
    }

    /**
     * Configures an event data builder to host a JSON payload.
     * @param eventId event's id.
     * @param eventType event's type.
     * @param eventData event's payload.
     * @return an event data builder.
     * @param <A> a type that can be serialized in JSON.
     */
    @Deprecated
    public static <A> EventDataBuilder builderAsJson(UUID eventId, String eventType, A eventData) {
        return EventDataBuilder.json(eventId, eventType, eventData);
    }

    /**
     * Configures an event data builder to host a JSON payload.
     * @param eventType event's type.
     * @param eventData event's payload.
     * @return an event data builder.
     */
    public static EventDataBuilder builderAsJson(String eventType, byte[] eventData) {
        return EventDataBuilder.json(eventType, eventData);
    }

    /**
     * Configures an event data builder to host a JSON payload.
     * @param eventId event's id.
     * @param eventType event's type.
     * @param eventData event's payload.
     * @return an event data builder.
     */
    public static EventDataBuilder builderAsJson(UUID eventId, String eventType, byte[] eventData) {
        return EventDataBuilder.json(eventId, eventType, eventData);
    }

    /**
     * Configures an event data builder to host a binary payload.
     * @param eventType event's type.
     * @param eventData event's payload.
     * @return an event data builder.
     */
    public static EventDataBuilder builderAsBinary(String eventType, byte[] eventData) {
        return builderAsBinary(null, eventType, eventData);
    }

    /**
     * Configures an event data builder to host a binary payload.
     * @param eventId event's id.
     * @param eventType event's type.
     * @param eventData event's payload.
     * @return an event data builder.
     */
    public static EventDataBuilder builderAsBinary(UUID eventId, String eventType, byte[] eventData) {
        return EventDataBuilder.binary(eventId, eventType, eventData);
    }
    
    public MessageData toMessageData() {
        return new MessageData(eventType, eventData, userMetadata, eventId, contentType);
    }
}

