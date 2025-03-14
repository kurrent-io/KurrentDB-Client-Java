package io.kurrent.dbclient;

import java.util.UUID;

/**
 * Represents a message that will be sent to KurrentDB.
 */
public final class MessageData {
    private final UUID messageId;
    private final String messageType;
    private final String contentType;
    private final byte[] messageData;
    private final byte[] messageMetadata;

    MessageData(String messageType, byte[] messageData) {
        this(messageType, messageData, null, UUID.randomUUID(), ContentType.JSON);
    }

    MessageData(String messageType, byte[] messageData, byte[] userMetadata) {
        this(messageType, messageData, userMetadata, UUID.randomUUID(), ContentType.JSON);
    }
    
    MessageData(String messageType, byte[] messageData, byte[] userMetadata, UUID messageId, String contentType) {
        this.messageId = messageId;
        this.messageType = messageType;
        this.contentType = contentType;
        this.messageData = messageData;
        this.messageMetadata = userMetadata;
    }

    /**
     * Returns message's unique identifier
     */
    public UUID getMessageId() {
        return messageId;
    }

    /**
     * Returns message's type.
     */
    public String getMessageType() {
        return messageType;
    }

    /**
     * Returns message's content's type
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Returns message's payload data
     */
    public byte[] getMessageData() {
        return messageData;
    }

    /**
     * Returns message's custom user metadata.
     */
    public byte[] getMessageMetadata() {
        return messageMetadata;
    }

    /**
     * Configures a message data builder to host a JSON payload.
     * @param messageType message's type.
     * @param messageData message's payload.
     * @return a message data builder.
     */
    public static MessageDataBuilder builderAsJson(String messageType, byte[] messageData) {
        return MessageDataBuilder.json(messageType, messageData);
    }

    /**
     * Configures a message data builder to host a JSON payload.
     * @param messageType message's type.
     * @param messageData message's payload.
     * @param messageMetadata message's metadata payload.
     * @return a message data builder.
     */
    public static MessageDataBuilder builderAsJson(String messageType, byte[] messageData, byte[] messageMetadata) {
        return MessageDataBuilder.json(messageType, messageData, messageMetadata);
    }

    /**
     * Configures a message data builder to host a binary payload.
     * @param messageType message's type.
     * @param messageData message's payload.
     * @return a message data builder.
     */
    public static MessageDataBuilder builderAsBinary(String messageType, byte[] messageData) {
        return MessageDataBuilder.binary(messageType, messageData);
    }

    /**
     * Configures a message data builder to host a binary payload.
     * @param messageType message's type.
     * @param messageData message's payload.
     * @return a message data builder.
     */
    public static MessageDataBuilder builderAsBinary(String messageType, byte[] messageData, byte[] messageMetadata) {
        return MessageDataBuilder.binary(messageType, messageMetadata);
    }
}
