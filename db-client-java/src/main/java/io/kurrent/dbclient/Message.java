package io.kurrent.dbclient;

import java.util.Objects;
import java.util.UUID;

/**
 * Represents a message wrapper in the KurrentDB system, containing both domain data and optional metadata.
 * Messages can represent events, commands, or other domain objects along with their associated metadata.
 */
public final class Message {
    private final Object data;
    private final Object metadata;
    private final UUID messageId;

    /**
     * Creates a new Message with the specified properties.
     *
     * @param data The message domain data.
     * @param metadata Optional metadata providing additional context about the message, such as correlation IDs, timestamps, or user information.
     * @param messageId Unique identifier for this specific message instance.
     */
    public Message(Object data, Object metadata, UUID messageId) {
        this.data = data;
        this.metadata = metadata;
        this.messageId = messageId;
    }

    /**
     * Creates a new Message with the specified domain data and random ID, but without metadata.
     * This factory method is a convenient shorthand when working with systems that don't require metadata.
     *
     * @param data The message domain data.
     * @return A new immutable Message instance containing the provided data and ID with null metadata.
     *
     * <p>Example:
     * <pre>
     * // Create a message with a specific ID
     * UserRegistered userRegistered = new UserRegistered("123", "Alice");
     * Message message = Message.from(userRegistered);
     * </pre>
     */
    public static Message from(Object data) {
        return from(data, null);
    }

    /**
     * Creates a new Message with the specified domain data and message ID, but without metadata.
     * This factory method is a convenient shorthand when working with systems that don't require metadata.
     *
     * @param data The message domain data.
     * @param messageId Unique identifier for this message instance. Must not be a nil UUID.
     * @return A new immutable Message instance containing the provided data and ID with null metadata.
     *
     * <p>Example:
     * <pre>
     * // Create a message with a specific ID
     * UserRegistered userRegistered = new UserRegistered("123", "Alice");
     * UUID messageId = UUID.randomUUID();
     * Message message = Message.from(userRegistered, messageId);
     * </pre>
     */
    public static Message from(Object data, UUID messageId) {
        return from(data, null, messageId);
    }

    /**
     * Creates a new Message with the specified domain data and message ID and metadata.
     *
     * @param data The message domain data.
     * @param metadata Optional metadata providing additional context about the message, such as correlation IDs, timestamps, or user information.
     * @param messageId Unique identifier for this specific message instance. If null, a random UUID will be generated.
     * @return A new immutable Message instance with the specified properties.
     * @throws IllegalArgumentException Thrown when messageId is explicitly set to a nil UUID, which is an invalid identifier.
     *
     * <p>Example:
     * <pre>
     * // Create a message with data and metadata
     * OrderPlaced orderPlaced = new OrderPlaced("ORD-123", 99.99);
     * EventMetadata metadata = new EventMetadata(
     *     "user-456", 
     *     Instant.now(),
     *     correlationId
     * );
     *
     * // Let the system assign an ID automatically
     * Message message = Message.from(orderPlaced, metadata);
     *
     * // Or specify a custom ID
     * Message messageWithId = Message.from(orderPlaced, metadata, UUID.randomUUID());
     * </pre>
     */
    public static Message from(Object data, Object metadata, UUID messageId) {
        if (messageId != null && messageId.equals(new UUID(0, 0))) {
            throw new IllegalArgumentException("Message ID cannot be a nil UUID.");
        }

        return new Message(data, metadata, messageId != null ? messageId : UUID.randomUUID());
    }

    /**
     * Gets the message domain data.
     *
     * @return The message domain data.
     */
    public Object data() {
        return data;
    }

    /**
     * Gets the message metadata.
     *
     * @return The message metadata, may be null.
     */
    public Object metadata() {
        return metadata;
    }

    /**
     * Gets the unique identifier for this message.
     *
     * @return The message ID.
     */
    public UUID messageId() {
        return messageId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;
        return Objects.equals(data, message.data) &&
                Objects.equals(metadata, message.metadata) &&
                Objects.equals(messageId, message.messageId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(data, metadata, messageId);
    }

    @Override
    public String toString() {
        return "Message{" +
                "data=" + data +
                ", metadata=" + metadata +
                ", messageId=" + messageId +
                '}';
    }
}
