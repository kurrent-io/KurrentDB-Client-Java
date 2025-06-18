package io.kurrent.dbclient;

import java.util.UUID;

/**
 * Utility class to help building an <i>MessageData</i>.
 */
public class MessageDataBuilder {
    private String messageType;
    private byte[] messageData;
    private byte[] messageMetadata;
    private UUID messageId;
    private String contentType;

    MessageDataBuilder() {
    }

    /**
     * Configures a message data builder to host a JSON payload.
     *
     * @param messageType message's type.
     * @param messageData message's payload.
     * @return a message data builder.
     */
    public static MessageDataBuilder json(String messageType, byte[] messageData) {
        return json(messageType, messageData, null, null);
    }

    /**
     * Configures a message data builder to host a JSON payload.
     *
     * @param messageType message's type.
     * @param messageData message's payload.
     * @param messageMetadata message's metadata payload.
     * @return a message data builder.
     */
    public static MessageDataBuilder json(String messageType, byte[] messageData, byte[] messageMetadata) {
        return json(messageType, messageData, messageMetadata, null);
    }

    /**
     * Configures a message data builder to host a JSON payload.
     *
     * @param messageId   message's id.
     * @param messageType message's type.
     * @param messageData message's payload.
     * @param messageMetadata message's metadata payload.
     * @return a message data builder.
     */
    public static MessageDataBuilder json(String messageType, byte[] messageData, byte[] messageMetadata, UUID messageId) {
        MessageDataBuilder self = new MessageDataBuilder();

        self.messageType = messageType;
        self.messageData = messageData;
        self.messageMetadata = messageMetadata;
        self.messageId = messageId;
        self.contentType = ContentType.JSON;

        return self;
    }

    /**
     * Configures a message data builder to host a binary payload.
     *
     * @param messageType message's type.
     * @param messageData message's payload.
     * @return a message data builder.
     */
    public static MessageDataBuilder binary(String messageType, byte[] messageData) {
        return binary(messageType, messageData, null, null);
    }

    /**
     * Configures a message data builder to host a binary payload.
     *
     * @param messageType message's type.
     * @param messageData message's payload.
     * @param messageMetadata message's metadata payload.
     * @return a message data builder.
     */
    public static MessageDataBuilder binary(String messageType, byte[] messageData, byte[] messageMetadata) {
        return binary(messageType, messageData, messageMetadata, null);
    }

    /**
     * Configures a message data builder to host a binary payload.
     *
     * @param messageId   message's id.
     * @param messageType message's type.
     * @param messageData message's payload.
     * @param messageMetadata message's metadata payload.
     * @return a message data builder.
     */
    public static MessageDataBuilder binary(String messageType, byte[] messageData, byte[] messageMetadata, UUID messageId) {
        MessageDataBuilder self = new MessageDataBuilder();

        self.messageType = messageType;
        self.messageData = messageData;
        self.messageId = messageId;
        self.messageMetadata = messageMetadata;
        self.contentType = ContentType.BYTES;

        return self;
    }


    /**
     * Configures a message data builder to host a binary payload.
     *
     * @param messageId   message's id.
     * @param messageType message's type.
     * @param messageData message's payload.
     * @param messageMetadata message's metadata payload.
     * @return a message data builder.
     */
    public static MessageDataBuilder with(String messageType, byte[] messageData, byte[] messageMetadata, UUID messageId, boolean isJson) {
        MessageDataBuilder self = new MessageDataBuilder();

        self.messageType = messageType;
        self.messageData = messageData;
        self.messageId = messageId;
        self.messageMetadata = messageMetadata;
        self.contentType = isJson ? ContentType.JSON : ContentType.BYTES;

        return self;
    }

    /**
     * Sets message's unique identifier.
     */
    public MessageDataBuilder messageId(UUID messageId) {
        this.messageId = messageId;
        return this;
    }

    /**
     * Sets message's custom user metadata.
     */
    public MessageDataBuilder messageMetadata(byte[] value) {
        this.messageMetadata = value;
        return this;
    }

    /**
     * Builds a message ready to be sent to KurrentDB.
     *
     * @see MessageData
     */
    public MessageData build() {
        UUID messageId = this.messageId == null ? UUID.randomUUID() : this.messageId;
        return new MessageData(this.messageType, this.messageData, this.messageMetadata, messageId, this.contentType);
    }
}
