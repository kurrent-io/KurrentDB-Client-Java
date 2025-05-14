package io.kurrent.dbclient.v2;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CancellationException;

/**
 * Defines the interface for a schema serializer.
 */
public interface SchemaSerializer {
    /**
     * The type of the schema.
     *
     * @return The data format of the schema.
     */
    SchemaDataFormat getDataFormat();

    /**
     * Serializes the given value into bytes according to the provided context.
     *
     * @param value The object to be serialized.
     * @param context The context providing additional information for the serialization process.
     * @return A CompletableFuture that resolves to a ByteBuffer containing the serialized data.
     */
    CompletableFuture<ByteBuffer> serialize(Object value, SerializationContext context);

    /**
     * Deserializes the given bytes into an object according to the provided context.
     *
     * @param data The bytes to be deserialized.
     * @param context The context providing additional information for the deserialization process.
     * @return A CompletableFuture that resolves to the deserialized object.
     */
    CompletableFuture<Object> deserialize(ByteBuffer data, SerializationContext context);

    /**
     * Serializes the given message.
     *
     * @param message The message to be serialized.
     * @return A CompletableFuture that resolves to a ByteBuffer containing the serialized data.
     */
    CompletableFuture<ByteBuffer> serialize(Message message);
}